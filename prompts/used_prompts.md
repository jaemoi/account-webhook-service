# AI 사용 프롬프트 기록 (used_prompts.md)

본 프로젝트는 ChatGPT를 활용하여 설계 검토, 코드 구조 개선, 테스트 작성 보조를 수행하였습니다.
아래는 실제 개발 과정에서 사용한 주요 프롬프트 요약입니다.

1. 구조설계
   schema.sql 처럼 테이블을 설계했고 요구사항 내용이 다음과 같은데 프로젝트 구조를
   어떻게 설정하면 좋을거같나요? 과제에서 멀티 모듈 구조를 쓸 수는 없지만 webhook api구현을 위한
   clean 아키텍처가 필요합니다. controller, service, domain, infra, 구조가 명확했음 좋겠고
   webhook 이벤트 저장 후 비동기 처리 구조를 설계하고 싶습니다.

2. Webhook 처리 설계
   Webhook 특성상 동일 이벤트가 여러 번 재전송될 수 있는데,
   eventId 기준으로 멱등성을 보장하려면 어떤 방식이 좋을까요?

DB unique 제약을 사용하는 방식과 애플리케이션 레벨 처리 중 어떤 방식이 더 안전한지,
그리고 RECEIVED → PROCESSING → DONE / FAILED 상태 전이를 어떻게 관리하는 것이
실무적으로 적절한 구조인지 알려주세요.

3. HMAC Signature 검증 구현
   Webhook 요청의 보안을 위해 HmacSHA256 기반 signature 검증을 구현하려고 합니다.
   Kotlin Spring Boot 환경에서 raw request body 기준으로 signature를 검증하는 방법과,
   헤더에 sha256 prefix가 포함되는 경우 어떻게 처리하는 것이 좋은지 알려주세요.

4. 이벤트 타입별 처리 구조 설계
   EMAIL_FORWARDING_CHANGED, ACCOUNT_DELETED, SOCIAL_ACCOUNT_DELETED 처럼
   이벤트 타입이 늘어날 수 있는데 if-else 분기 대신 확장 가능한 구조로 만들고 싶습니다.

이벤트별 처리 로직을 분리하기 위해 Strategy 패턴 또는 dispatcher 구조를
어떻게 설계하면 좋은지, EventProcessor 인터페이스 기반 구조를 적용해서 알려주세요.

5. 테스트 코드 및 통합 테스트 구성
   SpringBootTest 환경에서 webhook 흐름 전체를 검증하는 통합 테스트를
   어떻게 작성하는 것이 좋은지 알려주세요 코드를 보고 제가 판단할게요.

특히,

- webhook 수신 → 이벤트 저장 → process 실행 → 상태 변경
  전체 흐름을 테스트하는 방법
- SQLite 테스트 환경 구성
- 비동기 처리 로직을 테스트에서 안정적으로 검증하는 방법 위주로 알려주세요.