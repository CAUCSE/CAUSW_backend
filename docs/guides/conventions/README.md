# 코드 컨벤션

CAUSW Backend 의 레이어별 / 횡단별 코드 작성 규약입니다.

## 문서

| 주제 | 문서 |
|------|------|
| Controller, v1/v2, DTO, ApiResponse | [api-layer.md](./api-layer.md) |
| Service, Reader/Writer, 트랜잭션, Validator | [service-layer.md](./service-layer.md) |
| Entity, JPA Auditing, Soft Delete, Repository, QueryDSL, Flyway | [persistence.md](./persistence.md) |
| ErrorCode, GlobalExceptionHandler | [exception.md](./exception.md) |
| 포맷터, Lombok, 명명, Git 컨벤션 | [code-style.md](./code-style.md) |

## 핵심 원칙 요약

1. **버전 분리**: v1 (레거시) / v2 (현재 표준). 신규 기능은 v2.
2. **레이어 책임 분리**: Controller → Service → Reader/Writer/Validator/Manager 등 → Repository → Entity
3. **응답 표준화**: 모든 응답은 `ApiResponse<T>` 로 래핑
4. **예외 표준화**: 비즈니스 예외는 `{Domain}ErrorCode` enum + `toBaseException()`
5. **PK 표준화**: 모든 엔티티는 UUID String PK (`BaseEntity` 상속)
6. **소프트 삭제**: 신규 엔티티는 `deletedAt` (LocalDateTime), 기존 다수 엔티티는 `isDeleted` (Boolean). 물리 삭제 금지
7. **MapStruct 매퍼**: Controller ↔ Service 사이 변환은 `*DtoMapper` 가 담당
8. **Lombok 보수적 사용**: `@Data` X, `@Setter` 엔티티에는 X
9. **Naver Eclipse 포맷터 + Spotless** 적용

## 빠른 참고

### 신규 v2 API 추가 순서

1. `domain/{domain}/{sub}/entity/` 에 엔티티 추가 (`BaseEntity` 상속)
2. Flyway 마이그레이션 작성 (`./gradlew flywayCreate -Pdesc=...`)
3. `repository/` 에 `{Entity}Repository` (필요 시 `repository/query/{Entity}QueryRepository`)
4. `implementation/` 디렉터리에 `{Entity}Reader` / `{Entity}Writer` 등 컴포넌트 — 위치는 도메인 컨벤션에 따라 `service/implementation/` 또는 `service/v2/implementation/`
5. `service/v2/{Entity}Service` (트랜잭션 경계)
6. `service/v2/dto/` (또는 `service/dto/`) 에 Command/Query/Result DTO
7. `api/v2/dto/request/`, `api/v2/dto/response/` 에 Request/Response DTO
8. `api/v2/mapper/{Entity}DtoMapper` (MapStruct)
9. `api/v2/controller/{Entity}Controller` (`/api/v2/{리소스}`)
10. `shared/exception/errorcode/{Domain}ErrorCode` 에 필요한 에러 코드 추가

### 자주 쓰는 명령어

```bash
./gradlew spotlessApply        # 포맷 적용
./gradlew clean build          # 빌드 + 테스트
./gradlew flywayCreate -Pdesc=Name   # 마이그레이션 파일 생성
./gradlew flywayInfo           # 마이그레이션 상태 조회
./gradlew :app-main:bootRun    # 로컬 실행
```
