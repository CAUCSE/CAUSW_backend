# 예외 처리 컨벤션

비즈니스 예외를 어떻게 정의하고, 어떻게 던지며, 어떤 응답으로 변환되는지에 대한 규약입니다.

## 1. 예외 클래스 위치

모듈 분리에 맞춰 예외 클래스가 두 곳에 나뉘어 있지만, 둘 다 같은 `GlobalV2ExceptionHandler` 가 처리합니다.

| 위치 | 사용 시점 |
|------|----------|
| `global/exception` | `app-main` 외부에서도 쓸 수 있는 기초 예외 (`BaseRuntimeException`, `BadRequestException` 등). 일부 `core/security`, 보조 Validator/유틸 코드가 직접 사용 |
| `app-main/shared/exception` | 도메인 ErrorCode 표준. **신규 비즈니스 예외는 항상 이쪽** — `shared/exception/errorcode/{Domain}ErrorCode` 추가 |

## 2. 표준 패턴: `BaseResponseCode` + `BaseRunTimeV2Exception`

위치: `shared/exception/`. `BaseResponseCode` 인터페이스는 `getCode()` / `getMessage()` / `getStatus()` 와 `toBaseException()` default 메서드를 제공하고, 도메인 ErrorCode enum 이 이를 구현합니다.

```java
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum UserErrorCode implements BaseResponseCode {
    INVALID_PASSWORD_REQUEST(HttpStatus.BAD_REQUEST, "USER_400_001", "비밀번호 형식이 잘못되었습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_001", "존재하지 않는 사용자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
    // getCode/getMessage/getStatus 구현
}
```

명명 규약:
- Enum 클래스: `{Domain}ErrorCode` (도메인 또는 서브도메인 단위 — 엔티티와 1:1 아님. 예: `Report` 엔티티 하나에 `PostReportErrorCode`/`CommentReportErrorCode`/`ChildCommentReportErrorCode` 가 따로 존재)
- Enum 상수: `SCREAMING_SNAKE_CASE`
- `code`: `{DOMAIN}_{HTTP_STATUS}_{nnn}` (예: `USER_404_001`)
- `message`: 한국어, 마침표로 끝남, 사용자에게 그대로 노출되므로 친절하게 작성

기존 묶음은 `shared/exception/errorcode/` 디렉터리에서 도메인별로 확인.

## 3. 예외 던지는 패턴

```java
public Post getById(String id) {
    return postRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(PostErrorCode.POST_NOT_FOUND::toBaseException);
}
```

- 도메인 코드 안에서는 **항상 `*ErrorCode.toBaseException()`** 으로 throw, 직접 `new BaseRunTimeV2Exception(...)` 하지 않음
- Optional 조회 실패는 `.orElseThrow(*ErrorCode.*::toBaseException)`
- try/catch 로 잡지 않고 `GlobalV2ExceptionHandler` 가 처리하도록 위임
- 일부 보조 Validator(`AbstractValidator` 구현체)는 `global.exception` 의 `BadRequestException` 등을 직접 throw 하는 레거시 패턴도 있음 — 신규 코드는 `*ErrorCode.toBaseException()` 을 따름

## 4. 글로벌 예외 핸들러

위치: `shared/exception/GlobalV2ExceptionHandler` (`@RestControllerAdvice`)

| 예외 | HTTP | 응답 |
|------|------|------|
| `BaseRunTimeV2Exception` | `errorCode.status` | `{ code, message, data: null }` |
| `MethodArgumentNotValidException` (`@Valid` 실패) | 400 | `GlobalErrorCode.BAD_REQUEST` + 검증 메시지 |
| `BadRequestException`/`UnauthorizedException`/`ForbiddenException`/`ServiceUnavailableException`/`BaseRuntimeException` (`global.exception`) | 각자 status | 대응하는 `GlobalErrorCode` |
| `HttpMessageNotReadableException` (JSON 파싱 실패) | 400 | 친화적 메시지 |
| `Exception` (기타) | 500 | `GlobalErrorCode.INTERNAL_SERVER_ERROR` |

로깅: 4xx → `WARN` (스택트레이스 없이), 5xx → `ERROR` (스택트레이스 포함)

## 5. 체크리스트 (신규 예외 추가 시)

- [ ] 어느 도메인의 ErrorCode 인지 결정 → 없으면 새 `{Domain}ErrorCode` 추가
- [ ] enum 상수 정의: `(HttpStatus.*, "{DOMAIN}_{STATUS}_{NNN}", "{한국어 메시지}.")`
- [ ] 같은 status / 같은 도메인에서 다음 번호로 NNN 부여
- [ ] 코드에서 `*ErrorCode.*.toBaseException()` 으로 throw
- [ ] `GlobalV2ExceptionHandler` 가 처리하도록 위임 (try/catch 금지)
- [ ] 메시지가 사용자에게 그대로 노출됨을 의식하고 톤/맞춤법 점검
