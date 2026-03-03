package com.assignment.accountchange.application

import com.assignment.accountchange.application.service.InboxEventProcessorService
import com.assignment.accountchange.config.TestConfig
import com.assignment.accountchange.domain.model.AccountStatus
import com.assignment.accountchange.domain.model.Provider
import com.assignment.accountchange.domain.model.SocialAccountStatus
import com.assignment.accountchange.infra.persistence.repository.AccountRepository
import com.assignment.accountchange.infra.persistence.repository.InboxEventRepository
import com.assignment.accountchange.infra.persistence.repository.SocialAccountRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig::class)
@Transactional
class WebhookFacadeIntegrationTest {

    @Autowired
    lateinit var webhookFacade: WebhookFacade

    @Autowired
    lateinit var inboxEventRepository: InboxEventRepository

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var processorService: InboxEventProcessorService

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun cleanDb() {
        jdbcTemplate.update("DELETE FROM inbox_events")
        jdbcTemplate.update("DELETE FROM accounts")
    }

    /**
     * ✅ Idempotency 검증
     */
    @Test
    fun `duplicate eventId should not insert twice`() {

        val body = """
            {
              "eventType":"ACCOUNT_DELETED",
              "accountKey":"user1"
            }
        """.trimIndent()

        webhookFacade.receive("event-1", "sig", body)
        val result = webhookFacade.receive("event-1", "sig", body)

        assertEquals(WebhookHandleResult.InProgress, result)
    }

    /**
     * ✅ ACCOUNT_DELETED 처리 검증
     */
    @Test
    fun `account deleted event should mark account deleted`() {

        val body = """
            {
              "eventType":"ACCOUNT_DELETED",
              "accountKey":"user-del"
            }
        """.trimIndent()

        webhookFacade.receive("event-del", "sig", body)

        // async worker 실행
        processorService.processOne()

        val account = accountRepository.findByKey("user-del")

        assertNotNull(account)
        assertEquals(AccountStatus.DELETED, account!!.status)
    }

    /**
     * ✅ EMAIL_FORWARDING_CHANGED 처리 검증
     */
    @Test
    fun `email forwarding changed should update email`() {

        val body = """
        {
          "eventType":"EMAIL_FORWARDING_CHANGED",
          "accountKey":"user-email",
          "email":"new@test.com"
        }
        """.trimIndent()

        webhookFacade.receive("event-email", "sig", body)

        processorService.processOne()

        val account = accountRepository.findByKey("user-email")

        assertNotNull(account)
        assertEquals("new@test.com", account!!.email)
    }

    /**
     * ✅ invalid payload → FAILED 상태 저장 검증
     */
    @Test
    fun `invalid payload should throw exception`() {

        val body = """{ invalid json }"""

        assertThrows(com.fasterxml.jackson.core.JsonParseException::class.java) {
            webhookFacade.receive("event-fail", "sig", body)
        }

        // 저장되지 않았는지도 확인 (선택)
        val event =
            inboxEventRepository.findByEventId("event-fail")

        assertNull(event)
    }

    /**
     * ✅ 처리할 이벤트 없을 때
     */
    @Test
    fun `processOne should return nothing when no events`() {

        val result = processorService.processOne()

        assertTrue(
            result is InboxEventProcessorService.ProcessResult.NothingToProcess
        )
    }

    @Test
    fun `processed event should not process twice`() {

        val body = """
        {
          "eventType":"ACCOUNT_DELETED",
          "accountKey":"user-dup"
        }
    """.trimIndent()

        // 이벤트 수신
        webhookFacade.receive("event-dup", "sig", body)

        // 첫 처리 (정상 처리)
        val first = processorService.processOne()

        assertTrue(
            first is InboxEventProcessorService.ProcessResult.Processed
        )

        // 두 번째 처리 (더 이상 처리할 이벤트 없음)
        val second = processorService.processOne()

        assertTrue(
            second is InboxEventProcessorService.ProcessResult.NothingToProcess
        )
    }

    @Autowired
    lateinit var socialAccountRepository: SocialAccountRepository

    @Test
    fun `SOCIAL_ACCOUNT_DELETED should mark apple provider deleted`() {

        val body = """
        {
          "eventType":"SOCIAL_ACCOUNT_DELETED",
          "accountKey":"user-apple",
          "provider":"APPLE"
        }
    """.trimIndent()

        // webhook 수신
        webhookFacade.receive("event-apple", "sig", body)

        // worker 실행
        processorService.processOne()

        val socialAccounts =
            socialAccountRepository.findByAccountKey("user-apple")

        assertTrue(socialAccounts.isNotEmpty())

        val appleAccount =
            socialAccounts.first { it.provider == Provider.APPLE }

        assertEquals(
            SocialAccountStatus.DELETED,
            appleAccount.status
        )
    }
}