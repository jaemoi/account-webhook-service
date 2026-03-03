package com.assignment.accountchange.application

import com.assignment.accountchange.HiringAssignmentApplication
import com.assignment.accountchange.config.TestConfig
import com.assignment.accountchange.domain.model.AccountStatus
import com.assignment.accountchange.domain.model.EventStatus
import com.assignment.accountchange.infra.persistence.repository.AccountRepository
import com.assignment.accountchange.infra.persistence.repository.InboxEventRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(
    classes = [HiringAssignmentApplication::class]
)
@Import(TestConfig::class)
@Transactional
class WebhookFacadeIntegrationTest {

    @Autowired
    lateinit var webhookFacade: WebhookFacade

    @Autowired
    lateinit var inboxEventRepository: InboxEventRepository

    @Autowired
    lateinit var accountRepository: AccountRepository

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

    @Test
    fun `account deleted event should mark account deleted`() {

        val body = """
            {
              "eventType":"ACCOUNT_DELETED",
              "accountKey":"user-del"
            }
        """.trimIndent()

        webhookFacade.receive("event-del", "sig", body)

        val account = accountRepository.findByKey("user-del")

        assertNotNull(account)
        assertEquals(AccountStatus.DELETED, account!!.status)
    }

    @Test
    fun `invalid payload should store FAILED status`() {

        val body = """{ invalid json }"""

        assertThrows(Exception::class.java) {
            webhookFacade.receive("event-fail", "sig", body)
        }

        val status =
            inboxEventRepository.findStatusByEventId("event-fail")

        assertEquals(EventStatus.FAILED, status)
    }
}