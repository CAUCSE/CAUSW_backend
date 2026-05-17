# integration 도메인

위치: `app-main/src/main/java/net/causw/app/main/domain/integration`

## 책임

**외부 시스템과의 연동** 을 담당합니다. 학교 공지 크롤링, 데이터 Excel/CSV 내보내기 등 일회성/주기적 외부 입출력을 처리합니다.

## 서브 도메인

```
domain/integration/
├── crawled/    # 외부 공지사항 크롤링
└── export/     # 데이터 내보내기 (Excel / CSV)
```

## crawled — 공지사항 크롤링

`crawled/entity/` 하위:
- `CrawledNotice` — 크롤링된 외부 공지
- `CrawledPostImage` — 크롤링 첨부 이미지 메타
- `CrawledFileLink` — 크롤링된 파일 / 외부 링크

기술:
- `jsoup` — HTML 파싱
- Spring Batch / Scheduling — 주기 실행

흐름 (대표):
1. 스케줄러가 크롤링 잡 트리거
2. 학교 공지 사이트 HTML 가져와 jsoup 으로 파싱
3. `CrawledNotice` (+ 이미지/링크) 저장
4. 필요 시 `community/post` 의 `Post` 로 변환 (`isCrawled=true`)
5. 외부 사이트 갱신 감지 시 기존 레코드 업데이트

## export — 데이터 내보내기

기능:
- 학생회비 납부 명단, 회원 명단 등을 **Excel(`.xlsx`) / CSV** 로 내보내기
- 관리자 도구 / 대시보드에서 호출

기술:
- Apache POI — Excel 생성 (`poi-ooxml`)
- Apache Commons CSV — CSV 생성

응답:
- `Content-Type: application/octet-stream` + 파일 다운로드 헤더

## 외부 도메인 의존

| 의존 도메인 | 사용 |
|------------|------|
| community/post | 크롤링 결과 → 게시글 변환 |
| user/account | export 시 사용자 정보 |
| finance/usercouncilfee | 회비 내보내기 |
| campus/circle | 동아리 멤버 명단 등 |

## 주의 사항

- **외부 사이트 변경 시 크롤링 깨질 수 있음** — 실패 알림(`NotificationLog`) + 운영 디스코드 알림 연동 권장
- 크롤링은 트랜잭션 길게 잡지 말고, 항목 단위로 commit 하거나 chunk 처리
- 내보내기 작업은 메모리 사용량 주의 — 대량 데이터는 stream 기반 처리 권장 (POI `SXSSFWorkbook` 등)
- 외부 사이트 robots.txt / 크롤링 정책 준수
- HTTPS / 인증서 검증 (운영 환경에서는 검증 비활성화 금지)
- `spring-retry` 의존성은 추가되어 있으나 현재 `@EnableRetry`/`@Retryable` 미사용 — 외부 호출 재시도가 필요하면 활성화부터 검토
