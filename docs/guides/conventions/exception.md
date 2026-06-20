# 예외 처리 컨벤션

비즈니스 예외를 어떻게 정의하고, 어떻게 던지며, 어떤 응답으로 변환되는지에 대한 규약입니다.

## 1. 두 계층의 예외 클래스

모듈 분리에 맞춰 예외 클래스가 두 곳에 나뉘어 있지만, 둘 다 같은 `GlobalV2ExceptionHandler` 가 처리합니다.

| 위치 | 사용 시점 |
|------|----------|
| `global/exception` | `app-main` 외부에서도 쓸 수 있는 가장 기초적인 예외 (`BaseRuntimeException`, `BadRequestException`, `UnauthorizedException` 등). 일부 `core/security`, 도메인의 보조 Validator/유틸 코드에서 여전히 직접 사용됨 |
| `app-main/shared/exception` | 도메인 ErrorCode 표준. 신규 비즈니스 예외는 이쪽을 사용 |

신규 비즈니스 예외는 항상 `shared/exception/errorcode/{Domain}ErrorCode` 를 추가해 처리합니다.

## 2. 표준: `BaseResponseCode` + `BaseRunTimeV2Exception`

위치: `shared/exception/`

```java
public interface BaseResponseCode {
    String getCode();
    String getMessage();
    HttpStatus getStatus();
    default BaseRunTimeV2Exception toBaseException() {
        return new BaseRunTimeV2Exception(this);
    }
}
```

```java
@Getter
public class BaseRunTimeV2Exception extends RuntimeException {
    private final BaseResponseCode errorCode;

    public BaseRunTimeV2Exception(BaseResponseCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

## 3. 도메인별 ErrorCode 정의

위치: `shared/exception/errorcode/{Domain}ErrorCode`

```java
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum UserErrorCode implements BaseResponseCode {

    INVALID_PASSWORD_REQUEST(HttpStatus.BAD_REQUEST, "USER_400_001", "비밀번호 형식이 잘못되었습니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "USER_401_001", "이메일 또는 비밀번호가 잘못되었습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_001", "존재하지 않는 사용자입니다."),
    ALREADY_REGISTERED(HttpStatus.CONFLICT, "USER_409_001", "이미 가입된 계정입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override public String getCode()       { return code; }
    @Override public String getMessage()    { return message; }
    @Override public HttpStatus getStatus() { return status; }
}
```

### 명명 규약

| 부분 | 규칙 |
|------|------|
| Enum 클래스 | `{Domain}ErrorCode` (도메인 또는 서브도메인 단위) |
| Enum 상수 | `SCREAMING_SNAKE_CASE` (예: `USER_NOT_FOUND`) |
| `code` 문자열 | `{DOMAIN}_{HTTP_STATUS}_{nnn}` 형식 (예: `USER_404_001`) |
| `message` | 한국어. 마침표로 끝남. 사용자에게 그대로 보여지므로 친절하게 작성 |

### 기존 ErrorCode 묶음 (대표)

`shared/exception/errorcode/` 디렉터리에서 도메인 단위로 분할되어 있습니다. 새 도메인이 추가되면 그에 맞는 새 `*ErrorCode` 파일을 만듭니다.

| 묶음 | 파일 |
|--------|------|
| User | `UserErrorCode`, `UserInfoErrorCode`, `AuthErrorCode`, `BlockErrorCode`, `TermsErrorCode`, `AcademicRecordApplicationErrorCode` |
| Community | `PostErrorCode`, `CommentErrorCode`, `ChildCommentErrorCode`, `BoardErrorCode`, `BoardConfigErrorCode`, `LikePostErrorCode`, `PostReportErrorCode`, `CommentReportErrorCode`, `ChildCommentReportErrorCode`, `CeremonyErrorCode` |
| Asset | `LockerErrorCode` |
| Campus | `ScheduleErrorCode` |
| Notification | `NotificationLogErrorCode`, `NotificationSettingErrorCode` |
| Global | `GlobalErrorCode` |

`*ErrorCode` 분리 단위는 엔티티와 1:1 이 아닙니다. 예: `community/report` 의 entity 는 통합 `Report` 1개지만 `PostReportErrorCode`, `CommentReportErrorCode`, `ChildCommentReportErrorCode` 가 대상 유형별로 따로 존재합니다.

## 4. 예외 던지는 패턴

```java
public Post getById(String id) {
    return postRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(PostErrorCode.POST_NOT_FOUND::toBaseException);
}

public void validateOwner(Post post, String userId) {
    if (!post.getWriter().getId().equals(userId)) {
        throw PostErrorCode.NOT_POST_OWNER.toBaseException();
    }
}
```

규칙:
- 도메인 코드 안에서는 **항상 `*ErrorCode.toBaseException()` 메서드 체이닝** 으로 throw
- 직접 `throw new BaseRunTimeV2Exception(...)` 하지 않음 (`toBaseException()` 이 default 메서드로 제공)
- Optional 조회 실패는 `.orElseThrow(*ErrorCode.*::toBaseException)`
- 외부에서 잡기보다는 `GlobalV2ExceptionHandler` 가 통합 처리하도록 둡니다
- 일부 보조 Validator(`shared/AbstractValidator` 구현체 등)는 `global.exception` 의 `BadRequestException` 등을 직접 throw 하는 경우도 있음 — 신규 코드는 가능하면 `*ErrorCode.toBaseException()` 패턴을 따릅니다

## 5. 글로벌 예외 핸들러

위치: `shared/exception/GlobalV2ExceptionHandler`

```java
@RestControllerAdvice
public class GlobalV2ExceptionHandler {

    @ExceptionHandler(BaseRunTimeV2Exception.class)
    public ResponseEntity<ApiResponse<?>> handleBaseRunTimeV2Exception(BaseRunTimeV2Exception ex) {
        BaseResponseCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
            .body(ApiResponse.error(errorCode.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return ApiResponse.error(GlobalErrorCode.BAD_REQUEST.getCode(),
            ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> unknownException(Exception ex) {
        log.error("Internal server error", ex);
        return ApiResponse.error(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }
}
```

처리되는 예외 종류 (요약):

| 예외 | HTTP | 응답 |
|------|------|------|
| `BaseRunTimeV2Exception` | `errorCode.status` | `{ code, message, data: null }` |
| `MethodArgumentNotValidException` (`@Valid` 실패) | 400 | `GlobalErrorCode.BAD_REQUEST` + 검증 메시지 |
| `BadRequestException` (`global.exception`) | 400 | `GlobalErrorCode.BAD_REQUEST` |
| `UnauthorizedException` (`global.exception`) | 401 | `GlobalErrorCode.UNAUTHORIZED` |
| `ForbiddenException` (`global.exception`) | 403 | `GlobalErrorCode.FORBIDDEN` |
| `ServiceUnavailableException` (`global.exception`) | 503 | `GlobalErrorCode.SERVICE_UNAVAILABLE` |
| `BaseRuntimeException` (`global.exception`) | `errorCode.httpStatusCode` | `GlobalErrorCode.BAD_REQUEST` |
| `HttpMessageNotReadableException` (JSON 파싱 실패) | 400 | 날짜/시간 포맷 등 친화적인 메시지 |
| `Exception` (기타) | 500 | `GlobalErrorCode.INTERNAL_SERVER_ERROR` |

### 로깅 정책

- 4xx 클라이언트 에러 → `WARN` (스택트레이스 없이)
- 5xx 서버 에러 → `ERROR` (스택트레이스 포함)

## 6. 응답 예시

성공:
```json
{ "code": "S000", "message": "요청 처리 성공", "data": { ... } }
```

실패 (예: 비밀번호 형식 오류):
```json
{ "code": "USER_400_001", "message": "비밀번호 형식이 잘못되었습니다.", "data": null }
```

실패 (서버 에러):
```json
{ "code": "GLOBAL_500_001", "message": "서버 내부 오류가 발생했습니다.", "data": null }
```

## 7. 체크리스트 (신규 예외 추가 시)

- [ ] 어느 도메인의 ErrorCode 인지 결정 → 없으면 새 `{Domain}ErrorCode` 추가
- [ ] enum 상수 정의: `(HttpStatus.*, "{DOMAIN}_{STATUS}_{NNN}", "{한국어 메시지}.")`
- [ ] 같은 status / 같은 도메인에서 다음 번호로 NNN 부여
- [ ] 코드에서 `*ErrorCode.*.toBaseException()` 으로 throw
- [ ] `try/catch` 로 잡지 말고, `GlobalV2ExceptionHandler` 가 처리하도록 위임
- [ ] 메시지가 사용자에게 그대로 노출됨을 의식하고 톤/맞춤법 점검
