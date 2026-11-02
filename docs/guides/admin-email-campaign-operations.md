# 관리자 이메일 캠페인 운영 가이드

## 운영 활성화 전 AWS 준비

- SES 리전을 `EMAIL_CAMPAIGN_SES_REGION`과 일치시킨다.
- 발신 주소 또는 도메인의 SES identity 검증을 완료한다.
- SES sandbox를 해제하고 production access 승인을 확인한다.
- 계정의 일일 발송 한도와 `MaxSendRate`를 확인한다.
- 애플리케이션 EC2 Instance Profile에 `ses:SendEmail` 최소 권한을 부여한다.
- `EMAIL_CAMPAIGN_FROM_ADDRESS`와 필요 시 `EMAIL_CAMPAIGN_REPLY_TO_ADDRESS`를 설정한다.
- `EMAIL_CAMPAIGN_MAX_SEND_RATE`는 SES 계정의 `MaxSendRate` 이하로 설정한다.
- 설정과 검증이 끝나기 전에는 `EMAIL_CAMPAIGN_ENABLED=false`를 유지한다.

## 수동 smoke test

1. 실제 회원 전체가 아닌 운영자가 소유한 검증된 테스트 주소만 선택되는 조건을 준비한다.
2. 대상 미리보기 API로 주소 수와 분포를 확인한다.
3. 링크, 이미지, 기본 inline style이 포함된 소규모 캠페인을 생성한다.
4. 응답의 제목과 대상 수를 발송 확인 요청에 그대로 입력한다.
5. 캠페인 상세와 수신자 목록에서 `COMPLETED`/`SENT`, 시도 횟수, SES message ID 저장 여부를 확인한다.
6. CloudWatch와 애플리케이션 로그에서 throttle 또는 인증 오류가 없는지 확인한다.
7. 실패 수신자가 있다면 오류 코드만 확인하고 원본 이메일이나 HTML을 로그에 복사하지 않는다.

## 장애 및 복구

- 프로세스 재시작 후 `QUEUED`/`SENDING` 캠페인은 복구 스케줄러가 다시 처리한다.
- claim timeout을 지난 `SENDING` 수신자는 `PENDING`으로 반환된다.
- 단일 서버 기준 Guava RateLimiter와 Semaphore를 사용한다. 다중 인스턴스로 확장하기 전에는 분산 rate limit 또는 dispatcher 리더 선출로 교체해야 한다.
- 실제 발송을 즉시 멈추려면 `EMAIL_CAMPAIGN_ENABLED=false`로 재배포한다.
