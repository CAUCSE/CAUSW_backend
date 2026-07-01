# 서비스 레이어 컨벤션

`domain/{domain}/{sub}/service` 하위 코드 작성 규칙입니다.

## 1. 디렉터리 구조

표준 형태 (v1 은 모두 제거되어 서비스 레이어에는 버전 디렉터리가 없습니다):

```
service/
├── dto/                 # 서비스 계층 input/output DTO
├── mapper/              # 필요 시 매퍼
└── implementation/      # Reader / Writer / Validator 등 도메인 동작 컴포넌트
```

### 도메인별 변형

`implementation/` 의 위치는 거의 모든 도메인에서 `service/implementation/` 으로 통일되어 있습니다.

| 패턴 | 예 |
|------|-----|
| `service/implementation/` 직속 | 대부분의 서브 도메인 (`user/account`, `user/auth`, `community/post`, `asset/file`, `asset/locker` 등) |
| 별도 implementation 없이 `service/`에 `*Service.java` 직접 | `community/report` |
| `service/util/`, `service/mapper/`, `service/listener/`, `service/event/` 등 보조 패키지 추가 | `notification/notification`, `community/post` |

**원칙**: 새 서브 도메인을 만들 때는 비슷한 책임의 기존 서브 도메인을 따라가는 편이 안전합니다.

## 2. Service: 트랜잭션 경계

`service/` 아래 서비스 클래스는 비즈니스 흐름의 진입점이자 **트랜잭션 경계** 입니다.

```java
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostReader postReader;
    private final PostWriter postWriter;
    private final BoardReader boardReader;
    private final UserReader userReader;

    @Transactional
    public PostCreateResult createPost(PostCreateCommand command) {
        User writer = userReader.getById(command.writerId());
        Board board = boardReader.getById(command.boardId());
        // 검증 / 비즈니스 규칙
        Post saved = postWriter.create(...);
        return PostCreateResult.from(saved);
    }

    @Transactional(readOnly = true)
    public PostDetailResult getPost(PostDetailQuery query) { ... }
}
```

규칙:
- `@Service` + `@RequiredArgsConstructor`
- 메서드 단위 `@Transactional`. 조회는 `readOnly = true`
- **레포지토리를 직접 호출하지 않습니다.** `implementation/` 의 Reader / Writer 등을 통해서만 영속화 접근
- 인자/반환은 도메인 엔티티가 아닌 **서비스 DTO** (`*Command`, `*Query`, `*Result`)
- 다른 도메인의 Reader/Writer 도 주입 가능 (예: `PostService` 가 `UserReader` 사용)

### Service DTO 명명

| 패턴 | 용도 |
|------|------|
| `*Command` | 변경 요청 (생성/수정) input |
| `*Query` | 조회 요청 input |
| `*Result` | 서비스 반환 output (Controller 가 Response DTO 로 변환) |

위치는 도메인마다 `service/dto/` 또는 `service/dto/{request,response,result}/` 처럼 세분화될 수 있음.

## 3. implementation: 도메인 동작 컴포넌트 패턴

### 자주 쓰이는 명명 패턴

| 명명 | 책임 |
|------|------|
| `*Reader` | 조회 전용 (`find*`, `get*` 메서드) |
| `*Writer` | 변경 전용 (`create`, `update`, `softDelete` 등) |
| `*Validator` | 도메인 상태 검증 (예: `UserValidator`, `AdmissionValidator`) |
| `*Manager` | 복합 흐름 / 부수 작업 통합 (예: `PostImageManager`, `SocialAccountUnlinkManager`) |
| `*Creator` | 특정 생성 책임 (예: `UserInfoCreator`) |
| `*Linker` | 외부와의 연결 / 매핑 (예: `SocialAccountLinker`) |

### Reader 예시

```java
@Component
@RequiredArgsConstructor
public class PostReader {

    private final PostRepository postRepository;
    private final PostQueryRepository postQueryRepository;

    public Post getById(String id) {
        return postRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(PostErrorCode.POST_NOT_FOUND::toBaseException);
    }

    public Page<Post> findByBoardId(String boardId, Pageable pageable) {
        return postQueryRepository.findByBoardId(boardId, pageable);
    }
}
```

규칙:
- `@Component` (Service 가 아님)
- `find*` / `get*` 메서드만 정의
- `get*` 은 없을 경우 `*ErrorCode.toBaseException()` throw, `find*` 는 `Optional` 또는 `List` 반환
- `@Transactional` 은 호출자(Service) 의 트랜잭션을 그대로 따름 (별도 어노테이션 X)

### Writer 예시

```java
@Component
@RequiredArgsConstructor
public class PostWriter {

    private final PostRepository postRepository;

    public Post create(Post post) {
        return postRepository.save(post);
    }

    public void softDelete(Post post) {
        post.setIsDeleted(true);
    }
}
```

규칙:
- `@Component`
- `create`, `update`, `delete`(soft delete), `restore` 등 동사로 시작
- 영속화 후 엔티티 반환 또는 void
- 트랜잭션 시작 책임은 호출하는 Service 에 있음

분리 이유: 한 도메인의 영속화 / 도메인 동작을 책임별 컴포넌트로 나누면 SRP 가 지켜지고, 다른 도메인 서비스가 필요한 부분만 의존하면 됩니다.

## 4. 트랜잭션 정책

| 시나리오 | 권장 |
|---------|------|
| 단일 조회 / 목록 조회 | `@Transactional(readOnly = true)` |
| 생성 / 수정 / 삭제 | `@Transactional` (메서드 레벨) |
| 외부 API 호출 + DB 변경 혼합 | 가능하면 DB 변경을 트랜잭션 안, 외부 호출은 트랜잭션 밖으로 분리 |
| 비동기 처리 | `@Async` 메서드 (별도 트랜잭션 컨텍스트), 이벤트 발행 후 `@TransactionalEventListener` 활용 |

- 클래스 레벨 `@Transactional` 은 지양 (메서드별로 명시)
- isolation / propagation 은 기본값 사용. 명시할 경우 코드 리뷰에서 사유 설명

## 5. Validator 패턴

`shared/AbstractValidator` 를 상속하는 단순 검증 클래스 패턴이 일부에 남아 있습니다 (예: `user/account/util` 의 `UserStateValidator`, `PhoneNumberFormatValidator`).

```java
public class PhoneNumberFormatValidator extends AbstractValidator {
    private final String phoneNumber;

    private PhoneNumberFormatValidator(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static PhoneNumberFormatValidator of(String phoneNumber) {
        return new PhoneNumberFormatValidator(phoneNumber);
    }

    @Override
    public void validate() {
        if (!phoneNumber.matches("^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$")) {
            throw new BadRequestException(ErrorCode.INVALID_USER_DATA_REQUEST, "전화번호 형식이 잘못되었습니다.");
        }
    }
}
```

이 패턴의 `AbstractValidator` 구현체는 `*ErrorCode.toBaseException()` 대신 `global.exception` 의 `BadRequestException` 을 직접 throw 하는 예가 있습니다 — 신규 코드는 `shared/exception` 표준(§[exception.md](./exception.md))을 우선 검토하세요.

규칙:
- 단순 정규식/값 검증은 DTO 의 Jakarta validation 으로 처리
- 도메인 상태 / 다중 필드 검증은 `AbstractValidator` 구현체로 분리해 `validate()` 안에서 직접 호출
- 의존성(Repository 등)이 필요한 검증은 `@Component` 로 둔 별도 `*Validator` 클래스를 사용합니다 (예: `UserValidator`, `AdmissionValidator`). 위치는 보통 `service/implementation/`

## 6. 이벤트 발행

도메인 이벤트는 Spring `ApplicationEventPublisher` 로 발행하고, `@TransactionalEventListener` 또는 `@EventListener` 로 처리합니다.

예시:
- notification 도메인의 `service/listener/` 가 다양한 도메인 이벤트를 받아 알림으로 변환 (`@Async("asyncExecutor")` + `@TransactionalEventListener(phase = AFTER_COMMIT)`)
- `shared/infra/mail/event/MailEventListener` 가 이벤트 받아 메일 발송

원칙: 같은 도메인 안의 트랜잭션 동기 흐름은 직접 호출, 다른 도메인의 후속 작업은 이벤트로 분리.

## 7. 외부 도메인 의존

다른 도메인의 데이터가 필요하면 **해당 도메인의 Reader 를 주입**합니다.

```java
@Service
@RequiredArgsConstructor
public class PostService {
    private final UserReader userReader;     // user 도메인
    private final BoardReader boardReader;   // community/board
}
```

직접 다른 도메인의 Repository 를 호출하지 않습니다. (테스트 격리 / 책임 분리 목적)

## 8. 체크리스트 (신규 Service 추가 시)

- [ ] `service/{Entity}Service` 생성, `@Service` + `@RequiredArgsConstructor`
- [ ] 영속화 접근은 `implementation/` 의 `*Reader` / `*Writer` 통해서만 (위치는 같은 도메인 내 다른 서브 도메인을 따라감)
- [ ] Service 메서드 인자/반환은 도메인 엔티티가 아닌 `*Command` / `*Query` / `*Result` DTO
- [ ] 메서드 레벨 `@Transactional`, 조회는 `readOnly = true`
- [ ] 비즈니스 검증은 Validator 패턴 또는 도메인 헬퍼 컴포넌트
- [ ] 도메인 간 데이터는 외부 도메인의 Reader 를 주입해서 사용
- [ ] 영속화 실패 / 도메인 위반은 `*ErrorCode.toBaseException()` throw
