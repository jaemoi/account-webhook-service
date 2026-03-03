# Account Change Webhook Processing Server

Kotlin + Spring Boot + SQLite 기반의 Webhook 이벤트 처리 서버입니다.

외부 시스템(Apple/파트너/내부 시스템)에서 전달되는 계정 변경 이벤트를
안전하게 저장하고, 멱등성(Idempotency)을 보장하며, 계정 상태를 갱신하고
처리 결과를 조회할 수 있도록 구현되었습니다.

🧩 주요 기능

- Webhook 이벤트 수신 및 HMAC 서명 검증
- 이벤트 멱등 처리 (X-Event-Id 기반)
- Inbox Pattern 기반 이벤트 저장
- 비동기 처리 구조 (RECEIVED → PROCESSING → DONE / FAILED)
- 계정 상태 및 처리 결과 조회 API 제공

기술 스택

- Kotlin
- Spring Boot
- SQLite
- JdbcTemplate
- JUnit5 (Integration Test)

▶ 실행 방법

1. 프로젝트 실행

bash

./gradlew bootRun

서버실행 주소
http://localhost:8080

공통 설정

Webhook 요청에는 다음 Header가 필요합니다.

X-Event-Id : 이벤트 고유 ID
X-Signature : HMAC SHA256 서명

테스트용 secret 값은 다음과 같습니다.
secret = test-secret

서명 생성 공통 명령어
SIGNATURE=$(printf "%s" "$BODY" | openssl dgst -sha256 -hmac "test-secret" -hex | sed 's/^.* //')

1. EMAIL_FORWARDING_CHANGED (이메일 변경)

BODY='{"eventType":"EMAIL_FORWARDING_CHANGED","accountKey":"user-email","email":"new@test.com"}'
SIGNATURE=$(printf "%s" "$BODY" | openssl dgst -sha256 -hmac "test-secret" -hex | sed 's/^.* //')

curl -X POST http://localhost:8080/webhooks/account-changes \
-H "Content-Type: application/json" \
-H "X-Event-Id: event-apple" \
-H "X-Signature: $SIGNATURE" \
--data-raw "$BODY"

이벤트 처리 실행
curl -X POST http://localhost:8080/inbox/process

계정 조회
curl http://localhost:8080/accounts/user-email

2. ACCOUNT_DELETED (계정 삭제)

BODY='{"eventType":"ACCOUNT_DELETED","accountKey":"user-del"}'

SIGNATURE=$(printf "%s" "$BODY" | openssl dgst -sha256 -hmac "test-secret" -hex | sed 's/^.* //')

curl -X POST http://localhost:8080/webhooks/account-changes \
-H "Content-Type: application/json" \
-H "X-Event-Id: event-del" \
-H "X-Signature: $SIGNATURE" \
--data-raw "$BODY"

이벤트 처리 실행
curl -X POST http://localhost:8080/inbox/process

계정 상태 조회
curl http://localhost:8080/accounts/user-del

3. SOCIAL_ACCOUNT_DELETED (소셜 계정 삭제)

BODY='{"eventType":"SOCIAL_ACCOUNT_DELETED","accountKey":"user-google","provider":"GOOGLE"}'
SIGNATURE=$(printf "%s" "$BODY" | openssl dgst -sha256 -hmac "test-secret" -hex | sed 's/^.* //')

curl -X POST http://localhost:8080/webhooks/account-changes \
-H "Content-Type: application/json" \
-H "X-Event-Id: event-google-deleted" \
-H "X-Signature: $SIGNATURE" \
--data-raw "$BODY"

이벤트 처리 실행
curl -X POST http://localhost:8080/inbox/process

계정 조회 (소셜 계정 상태 확인)
curl http://localhost:8080/accounts/user-google

4. Idempotency 테스트 (동일 이벤트 재전송)
   최초 Webhook 전송

BODY='{"eventType":"ACCOUNT_DELETED","accountKey":"user-idem"}'
SIGNATURE=$(printf "%s" "$BODY" | openssl dgst -sha256 -hmac "test-secret" -hex | sed 's/^.* //')

curl -X POST http://localhost:8080/webhooks/account-changes \
-H "Content-Type: application/json" \
-H "X-Event-Id: event-idem-1" \
-H "X-Signature: $SIGNATURE" \
--data-raw "$BODY"

동일 eventId 다시 전송

curl -X POST http://localhost:8080/webhooks/account-changes \
-H "Content-Type: application/json" \
-H "X-Event-Id: event-idem-1" \
-H "X-Signature: $SIGNATURE" \
--data-raw "$BODY"

-> processing 응답이 옴

이벤트 처리 실행
curl -X POST http://localhost:8080/inbox/process

이벤트 상태 확인
curl http://localhost:8080/inbox/events/event-idem-1