# 크롤링 시스템 가이드

CAUSW 크롤링 시스템은 사이트별 HTML 파싱과 공통 수집·정제·저장 흐름을 분리한다. 현재 등록된 사이트는 중앙대학교 소프트웨어학부 공지사항이다.

## 전체 흐름

```text
CrawlScheduler
  → CrawlService
    → SiteConfigReader.findAllEnabled()
      → 활성 DB SiteConfig별 CrawlContext
    → SiteCrawler.fetchList(context)
      → CrawlHttpClient.fetch(listUrl)
      → List<ArticleUrl>
    → SiteCrawler.fetchArticle(context)
      → CrawlHttpClient.fetch(articleUrl)
      → HTML 파싱
      → RawArticle
    → CrawledArticleCleaner.clean()
      → CleanArticle
    → CrawledNoticeWriter.upsert()
      → CREATED / UPDATED / UNCHANGED
  → CrawledToPostTransferService
    → Post 생성 또는 수정
    → 신규 생성 시 PostCategoryClassifier로 성격 분류
    → CrawledNotice.post 연결
```

외부 HTTP 요청과 HTML 파싱은 DB 트랜잭션 밖에서 실행된다. `CrawledNoticeWriter` 메서드가 각 기사의 저장 트랜잭션을 담당한다.

## 코드 구조

```text
domain/integration/crawled/
├── client/                 # HTTP 요청, timeout, retry, delay
├── config/                 # crawler/pagination 유형, DB converter
├── core/                   # Pipeline, Scheduler, SiteCrawlerRegistry
├── crawler/                # SiteCrawler 계약과 사이트별 구현
├── dto/                    # ArticleUrl, RawArticle, CleanArticle, CrawlResult
├── entity/                 # SiteConfig, selector, CrawledNotice, 첨부파일, Post 외부 이미지
├── repository/
└── service/
    ├── CrawledArticleCleaner
    ├── CrawledToPostTransferService
    ├── PostCategoryBackfillService
    └── implementation/     # CrawledNoticeReader/Writer
```

## 사이트 추가

1. `SiteCrawler` 구현체를 `@Component`로 추가한다.
2. `tb_crawl_site_config`에 URL, 헤더, timeout, retry, selector와 `crawler_type`을 등록한다.
3. 구현체에 `CrawlHttpClient`를 주입하고 목록 요청에도 공통 클라이언트를 사용한다.
4. `getCrawlerType()`이 DB의 `crawler_type`과 일치하도록 설정한다. 크롤러는 `SiteConfig`를 직접 생성하지 않는다.
5. `fetchList(context)`는 URL과 사이트 내 외부 식별자를 담은 `ArticleUrl`을 반환한다.
6. `fetchArticle(context, ...)`는 공통 HTTP 클라이언트로 상세 HTML을 요청하고, 주입된 selector로 사이트 구조를 해석하여 `RawArticle`을 반환한다. 날짜 변환, HTML sanitize, URL 정규화, 해시 생성은 Cleaner에 맡긴다.
7. 활성화된 설정은 공통 `app.crawl.cron` 주기에 자동으로 수집되므로 사이트별 Scheduler 트리거는 추가하지 않는다.

`SiteCrawlerRegistry`는 Spring이 발견한 모든 `SiteCrawler`를 `CrawlerType`으로 등록한다. Pipeline은 `siteId`로 DB 설정을 읽고, 설정의 `crawlerType`에 맞는 구현체를 선택한다.

## SiteConfig 저장

`SiteConfig`는 `tb_crawl_site_config`에 저장되는 JPA 엔티티다. `target_board_id`로 수집 결과가 변환될 게시판을 지정하고, `request_headers`는 JSON으로 변환하며 selector는 동일 테이블의 embedded column으로 저장한다. Pipeline은 실행 시점에 활성 설정을 조회하여 `CrawlContext` 하나를 생성하고, 해당 실행의 목록·상세·정제 단계에 같은 설정을 전달한다.

## 식별과 변경 감지

`tb_crawled_notice` 행은 `(site_id, external_id)`로 식별한다. 중앙대 SW 공지는 목록 URL의 `code`와 `uid`를 조합한 `code:uid`를 `external_id`로 사용한다.

변경 해시에는 다음 값이 구분자와 함께 포함된다.

- 제목, 정제된 HTML, 작성자, 공지일
- 대표 이미지 URL
- URL로 정렬된 첨부파일명과 URL

해시가 다르면 엔티티의 모든 수집 필드와 첨부파일을 교체하고 `is_updated=true`로 설정한다.

## Post 변환

`CrawledNotice.target_board_id`는 수집 당시 `SiteConfig`의 저장 대상 게시판을 보존한다. Transfer는 게시판 이름을 사용하지 않고 이 ID로 Board를 조회한다. `post_id`는 원본 공지와 내부 Post를 직접 연결한다. 기존 데이터는 마이그레이션에서 제목이 유일하게 매칭되는 경우만 백필한다. 매핑되지 않은 기존 행은 첫 변환 시 제목으로 한 번 탐색한 후 `post_id`를 저장한다. 이후에는 제목이 바뀌어도 같은 Post를 수정한다.

## 성격 분류

Post를 새로 생성할 때만 `PostCategoryClassifier`가 제목 키워드로 성격(`Post.category`)을 지정한다. 규칙은 채용 → 대외활동 → 행사·특강 → 연구 → 학사 순으로 평가해 먼저 걸리는 카테고리로 확정하고, 어디에도 걸리지 않으면 미분류(null)로 남긴다. 기존 Post를 갱신할 때는 재분류하지 않는다. 관리자가 `PATCH /api/v2/admin/posts/{postId}/category`로 지정한 성격이 원문 수정 때마다 덮어써지는 것을 막기 위함이다. 목록의 `article_category_selector`가 수집하는 배지는 분류에 쓰지 않는다. 중앙대 SW 공지는 이 값이 모든 글에서 "공지"로 동일해 성격 신호가 되지 않기 때문이며, 주제 태그를 제공하는 사이트를 추가할 때 태그 기반 분류를 앞단에 두는 것을 검토한다.

이미 변환된 과거 공지는 `POST /api/v2/admin/crawled-notices/categories/backfill`로 소급 분류한다. 같은 분류기를 재사용하므로 규칙이 이원화되지 않으며, 성격이 이미 지정된 게시글은 대상에서 제외되므로 관리자가 지정한 값은 보존된다. 새 Post를 만들지 않아 공지 알림(`OfficialPostEvent`)은 발행되지 않고, 벌크 갱신을 사용해 `updated_at`도 변경하지 않는다.

## 실패 정책

- 목록 요청 실패: 해당 사이트 실행 중단
- 개별 상세 요청·파싱·정제·저장 실패: URL을 `CrawlResult.failedUrls` 에 기록하고 다음 기사 계속
- 중복 스케줄 실행: 현재 JVM에서 스킵

여러 서버 인스턴스가 동시에 Scheduler를 실행하는 배포에서는 DB/Redis 기반 분산 락이 추가로 필요하다.

## 설정

```yaml
app:
  crawl:
    enabled: true
    local-run-on-start: false
    zone: Asia/Seoul
    cron: "0 0 * * * *"
```

`application-local.yml`은 `local-run-on-start=true`로 오버라이드한다. 새 환경 변수는 추가되지 않았으므로 `.env.example` 수정은 필요 없다.

## 검증

```bash
./gradlew :app-main:test --tests "net.causw.app.main.domain.integration.crawled.*"
./gradlew :app-main:spotlessCheck
./gradlew :app-main:test --continue
./gradlew flywayValidate
```

이 기능은 DB 마이그레이션을 포함하므로 PR에 `db-change` 라벨이 필요하다.
