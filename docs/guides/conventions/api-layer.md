# API 레이어 컨벤션

`domain/{domain}/{sub}/api` 패키지에 위치하는 Controller, 요청/응답 DTO, Mapper 작성 규칙입니다.

## 1. 버저닝: v1 vs v2

이 프로젝트는 **두 개의 API 버전이 공존**합니다. 신규 기능은 항상 v2 로 작성합니다.

| 버전 | 경로 prefix | DTO 위치 | 특징 |
|------|-------------|----------|------|
| **v1 (레거시)** | `/api/v1/...` | `api/v1/dto/` | 일부 응답 포맷이 다름. 유지 보수만, 신규 기능 추가 X |
| **v2 (표준)** | `/api/v2/...` | `api/v2/dto/{request,response}/` | `ApiResponse<T>` 래퍼 사용. request/response 디렉터리 분리 |

### v1 → v2 마이그레이션 시

- 기존 v1 controller / service / DTO 는 유지한 채 v2 를 추가합니다.
- 같은 서브 도메인 안에서 `service/v1` 과 `service/v2` 가 공존할 수 있습니다.
- 클라이언트 호환성이 확보되어 deprecation 정책에 의해 제거될 때까지 v1 을 함부로 삭제하지 않습니다.

## 2. Controller 작성 규칙

### 어노테이션 / 경로 패턴

대표 패턴 (예: `community/post/api/v2/controller/PostController`):

```java
@RestController
@RequiredArgsConstructor
@Tag(name = "Post Public v2", description = "게시글 관련 API")
@RequestMapping("/api/v2/posts")
public class PostController {
    private final PostService postService;
    private final PostDtoMapper postDtoMapper;
}
```

규칙:
- `@RestController` + `@RequestMapping("/api/v{n}/{리소스명}")` 조합
- 클래스 이름: v2 는 `{Entity}Controller`, v1 은 `{Entity}V1Controller` 형태 (예: `PostController`, `PostV1Controller`)
- 관리자 전용 Controller 는 `{Entity}AdminController` 패턴이 발견됨 (`community/report/api/v2/controller/ReportAdminController` 등) — 별도 권한 검증 적용
- Swagger 태그: `@Tag(name = "{도메인} Public v{n}", description = "...")`
- 의존성은 모두 `private final` + `@RequiredArgsConstructor` 로 생성자 주입

### 메서드 정의

```java
@PostMapping(value = "/{id}/like")
@ResponseStatus(value = HttpStatus.CREATED)
@Operation(summary = "게시글 좋아요 저장 API", description = "...")
public ApiResponse<Void> likePost(
    @PathVariable("id") String id,
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    this.likePostService.likePost(userDetails.getUser().getId(), id);
    return ApiResponse.success();
}
```

규칙:
- 모든 응답은 `ApiResponse<T>` 로 래핑 (성공/실패 모두)
- 성공 시 `ApiResponse.success(data)` 또는 데이터가 없을 때 `ApiResponse.success()`
- HTTP 상태 코드는 `@ResponseStatus` 로 명시 (기본값 200 에 의존하지 않음)
- 인증된 사용자 정보는 `@AuthenticationPrincipal CustomUserDetails` 로 받음
- Swagger 문서를 위해 `@Operation(summary = ..., description = ...)` 추가
- 입력 검증은 `@Valid` 와 Jakarta Bean Validation 어노테이션 활용

### multipart/form-data

이미지 / 파일 업로드는 multipart 로 받습니다.

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ApiResponse<PostCreateResponse> createPost(
    @RequestPart("request") @Valid PostCreateRequest request,
    @RequestPart(value = "images", required = false) List<MultipartFile> images,
    @AuthenticationPrincipal CustomUserDetails userDetails) { ... }
```

- JSON 메타데이터는 `request` 파트, 파일은 별도 파트
- 파일 메타데이터(이미지 순서 등)는 request DTO 내부에 `images[].fileIndex` 식으로 매핑

## 3. 응답 포맷: `ApiResponse<T>`

위치: `shared/dto/ApiResponse`

```json
{
  "code": "S000",
  "message": "요청 처리 성공",
  "data": { ... }
}
```

- 모든 v2 응답은 이 구조를 따릅니다 (`@JsonInclude(JsonInclude.Include.NON_NULL)` 로 null 필드는 응답에서 제외).
- 성공 코드/메시지는 `shared/exception/ResponseCode` enum 의 `SUCCESS` 값을 사용 (`S000`)
- 실패: `code` 는 도메인별 `*ErrorCode` enum 의 값(예: `USER_404_001`), `message` 는 사람이 읽을 수 있는 한국어 메시지
- 페이징은 `shared/dto/PageResponse` 활용

예외 응답 흐름: [exception.md](./exception.md).

## 4. DTO 컨벤션

### 위치 / 명명

| 종류 | 위치 | 명명 |
|------|------|------|
| v2 Request DTO | `api/v2/dto/request/` | `{Action}{Entity}Request` (예: `PostCreateRequest`) |
| v2 Response DTO | `api/v2/dto/response/` | `{Entity}Response`, `{Action}{Entity}Response` (예: `PostResponse`, `PostCreateResponse`) |
| v1 DTO | `api/v1/dto/` | `{Entity}{Action}RequestDto`, `{Entity}{Action}ResponseDto` (Dto 접미사, request/response 디렉터리 분리 안 됨) |
| Service 계층 DTO | `service/{vN}/dto/` 또는 `service/dto/` | `{Entity}{Action}Query`, `{Entity}{Action}Result`, `{Entity}{Action}Command` |

### 작성 형식

대부분 Lombok 기반 class.

```java
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostCreateRequest {
    @NotBlank
    private String content;
    @NotNull
    private String boardId;
    private Boolean isAnonymous;
    private List<ImageMetadata> images;
}
```

- 검증: `@NotNull`, `@NotBlank`, `@Size`, `@Pattern` 등 Jakarta validation
- 기본 생성자는 `PROTECTED` 또는 `PRIVATE` 로 외부 직접 생성 차단
- `@Builder` 활용. `@AllArgsConstructor` 는 `PRIVATE` 권장
- DTO 내부 nested 클래스는 static class 로 정의

## 5. Mapper (MapStruct)

위치: `api/v{n}/mapper/`, 명명: `{Entity}DtoMapper` (v2) / `{Entity}DtoV1Mapper` (v1)

```java
@Mapper(componentModel = "spring")
public interface PostDtoMapper {
    PostCreateCommand toCreateCommand(PostCreateRequest request, String writerId);

    @Mapping(target = "boardName", source = "board.name")
    PostResponse toResponse(PostDetailResult result);
}
```

규칙:
- `@Mapper(componentModel = "spring")` 으로 Spring Bean 등록
- Controller ↔ Service 사이 변환은 Mapper 가 책임
- 복잡한 변환은 `default` 메서드 또는 `@AfterMapping`

MapStruct 1.4.x 는 최신 기능 일부가 빠져 있으니, 새 어노테이션을 도입하기 전에 컴파일 가능 여부 확인이 필요합니다.

## 6. 인증 / 인가

### `@PreAuthorize`

메서드 시큐리티가 활성화되어 있어 (`WebSecurityConfig` 의 `@EnableMethodSecurity`) Controller 메서드에 직접 사용 가능합니다.

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/...")
public ApiResponse<...> adminApi(...) { ... }
```

- 역할(Role): `domain/user/account/enums/user/Role` enum 의 값들 (예: `ADMIN`, `PRESIDENT`, `VICE_PRESIDENT`, `LEADER_*`, `COMMON`)
- 역할 그룹: `RoleGroup` enum
- 가능한 한 Controller 메서드에 명시 (서비스 레벨 검증은 별도 Validator 패턴 활용)

### 현재 사용자 정보

```java
@AuthenticationPrincipal CustomUserDetails userDetails
User user = userDetails.getUser();
```

위치: `domain/user/auth/userdetails/CustomUserDetails`

## 7. 예외 / 검증 실패

Controller 에서는 별도 예외를 잡지 않습니다. 모든 예외는 `shared/exception/GlobalV2ExceptionHandler` 가 통합 처리.

- `@Valid` 위반 → `MethodArgumentNotValidException` → 400 BAD_REQUEST (`GlobalErrorCode.BAD_REQUEST`)
- 비즈니스 예외 → `*ErrorCode.toBaseException()` throw → Handler 가 적절한 HTTP status 로 변환

자세한 예외 처리 흐름: [exception.md](./exception.md).

## 8. Swagger / OpenAPI

- `springdoc-openapi-starter-webmvc-ui` 사용
- 클래스 레벨: `@Tag(name, description)`
- 메서드 레벨: `@Operation(summary, description)`
- 응답 스키마: `@Schema(description, example)` (DTO 필드에 추가)
- 보안: `core/security/SwaggerSecurityConfig` 가 Swagger 경로 접근 권한 처리

## 9. 체크리스트 (신규 v2 Controller 추가 시)

- [ ] `api/v2/controller/{Entity}Controller` 생성, `/api/v2/{리소스}` 매핑
- [ ] `@Tag` + `@Operation` 추가
- [ ] 요청 DTO 는 `api/v2/dto/request/`, 응답 DTO 는 `api/v2/dto/response/`
- [ ] 인증 필요 시 `@AuthenticationPrincipal CustomUserDetails` 활용
- [ ] 역할 기반 인가는 `@PreAuthorize`
- [ ] 응답은 `ApiResponse.success(...)` 또는 `ApiResponse.success()` 래핑
- [ ] `@ResponseStatus` 명시
- [ ] 입력 검증은 `@Valid` + Bean Validation 어노테이션
- [ ] Mapper(`{Entity}DtoMapper`) 통해 Service 계층 DTO 로 변환 후 호출
