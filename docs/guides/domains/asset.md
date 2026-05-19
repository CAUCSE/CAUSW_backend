# asset 도메인

위치: `app-main/src/main/java/net/causw/app/main/domain/asset`

## 책임

**파일 업로드 / 다운로드 / 정리 + 사물함 관리** 를 책임집니다. 게시글 첨부, 프로필 이미지 등 다양한 곳에서 파일을 사용하는 거의 모든 도메인이 의존합니다.

## 서브 도메인

```
domain/asset/
├── file/      # 파일 추상화 (UuidFile, 조인 엔티티)
└── locker/    # 사물함 관리
```

## file — 파일

`file/entity/` 하위:
- `UuidFile` — 업로드된 파일의 추상 단위
  - UUID 파일명, S3 키, 원본 파일명, 확장자, 크기, 사용 위치 등
- `entity/joinEntity/` — 사용처별 조인 엔티티들 (예: `PostAttachImage` 가 `Post` ↔ `UuidFile` 을 연결)

비즈니스 규칙:
- 모든 첨부 파일은 `UuidFile` 로 통일된 메타 보유
- 사용처별 조인 엔티티를 통해 N:M 형태 매핑
- 미사용 `UuidFile` 은 배치로 정기 정리 (`core/batch/` 의 `CleanUnusedUuidFilesBatchConfig`)

저장소:
- AWS S3 (`shared/infra/storage/` 와 `shared/storage/{v1,v2}/`)
- 클라이언트 유틸: `S3Util`, 설정 Bean: `StorageConfig`

API:
- v1, v2 모두 — 파일 업로드 / URL 생성

서비스 위치:
- `file/service/v2/implementation/` 에 Reader/Writer 등 영속화 컴포넌트

## locker — 사물함

`locker/entity/` 하위:
- `Locker` — 사물함 정의 (호수 등)
- `LockerLog` — 신청 / 사용 이력
- `LockerLocation` — 사물함 위치 (건물 / 층)
- `LockerName` — 사물함 명명 관리
- `LockerStatus` — 사물함 상태 enum 또는 엔티티 (디렉터리 위치)

Repository:
- `locker/repository/`
- `locker/repository/query/` — QueryDSL 동적 쿼리 (예: 동적 필터 조건으로 사물함 검색)

서비스 위치:
- `locker/service/v1/`, `service/v1/validators/` — 레거시 검증
- `locker/service/v2/`, `service/v2/implementation/`, `service/v2/dto/result/`
- `locker/util/` — 도메인 보조 유틸

API:
- v1, v2 모두 존재
- 관리자 전용: `api/v2/controller/admin/` 하위에 별도 (요청/응답 DTO 와 매퍼도 별도 구성)

ErrorCode: `LockerErrorCode`

비즈니스 규칙 (대표):
- 1인 1사물함 원칙
- 학기 단위 신청 / 반납 워크플로
- 미반납 등 패널티 처리

## 외부 도메인 의존

| 의존 도메인 | 사용 |
|------------|------|
| user/account | 사물함 사용자, 파일 업로더 |
| community | 게시글/댓글 첨부 이미지 |
| campus/semester | 사물함 신청 학기 |

## 주의 사항

### 파일

- **업로드 → DB 저장 → 사용처 매핑** 의 3단계 흐름. 어느 단계에서 실패해도 고아 파일이 남을 수 있음 → 정기 정리 배치 필수
- 파일 확장자 검증 (`ErrorCode.INVALID_FILE_EXTENSION`)
- 업로드 실패 / 삭제 실패 시 `ErrorCode.FILE_UPLOAD_FAIL`, `FILE_DELETE_FAIL`
- 응답에서 직접 S3 키를 노출하지 말고 **public URL** 또는 **presigned URL** 변환 (`shared/dto/util/dtoMapper/custom/UuidFileToUrlDtoMapper`)
- 게시글 soft delete 시 첨부 파일은 즉시 제거되지 않음 — 별도 정책에 따름

### 사물함

- 동시 신청 경합 — 트랜잭션 격리 / 락 또는 단일 트랜잭션 내 검증으로 해결
- 자물쇠 번호 등 민감 정보는 노출 권한 점검
- 관리자 API 는 일반 사용자 API 와 별도 `admin/` 디렉터리로 분리되어 있음
