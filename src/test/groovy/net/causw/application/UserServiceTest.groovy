package net.causw.application

import net.causw.application.dto.DuplicatedCheckResponseDto
import net.causw.application.dto.board.BoardResponseDto
import net.causw.application.dto.circle.CircleResponseDto
import net.causw.application.dto.user.*
import net.causw.application.spi.*
import net.causw.config.JwtTokenProvider
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.*
import net.causw.infrastructure.GcpFileUploader
import net.causw.infrastructure.GoogleMailSender
import net.causw.infrastructure.PasswordGenerator
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.Validation
import javax.validation.Validator
import java.time.LocalDateTime

@ActiveProfiles(value = "test")
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest([UserDomainModel.class, UserAdmissionDomainModel.class, FavoriteBoardDomainModel.class])
class UserServiceTest extends Specification {
    private UserPort userPort = Mock(UserPort.class)
    private BoardPort boardPort = Mock(BoardPort.class)
    private PostPort postPort = Mock(PostPort.class)
    private UserAdmissionPort userAdmissionPort = Mock(UserAdmissionPort.class)
    private UserAdmissionLogPort userAdmissionLogPort = Mock(UserAdmissionLogPort.class)
    private CirclePort circlePort = Mock(CirclePort.class)
    private CircleMemberPort circleMemberPort = Mock(CircleMemberPort.class)
    private CommentPort commentPort = Mock(CommentPort.class)
    private FavoriteBoardPort favoriteBoardPort = Mock(FavoriteBoardPort.class)
    private LockerPort lockerPort = Mock(LockerPort.class)
    private LockerLogPort lockerLogPort = Mock(LockerLogPort.class)
    private JwtTokenProvider jwtTokenProvider = Mock(JwtTokenProvider.class)
    private GcpFileUploader gcpFileUploader = Mock(GcpFileUploader.class)
    private GoogleMailSender googleMailSender = Mock(GoogleMailSender.class)
    private PasswordGenerator passwordGenerator = Mock(PasswordGenerator.class)
    private PasswordEncoder passwordEncoder = Mock(PasswordEncoder.class)
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private SocialLoginFactory socialLoginFactory = Mock(SocialLoginFactory.class);
    private UserService userService = new UserService(
            this.userPort,
            this.boardPort,
            this.postPort,
            this.userAdmissionPort,
            this.userAdmissionLogPort,
            this.circlePort,
            this.circleMemberPort,
            this.commentPort,
            this.favoriteBoardPort,
            this.lockerPort,
            this.lockerLogPort,
            this.jwtTokenProvider,
            this.gcpFileUploader,
            this.googleMailSender,
            this.passwordGenerator,
            this.passwordEncoder,
            this.validator,
            this.socialLoginFactory
    )


    def mockUserDomainModel
    def mockUserAdmissionDomainModel
    def mockCircleDomainModel
    def mockCircleMemberDomainModel

    def setup() {
        this.mockUserDomainModel = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.mockUserAdmissionDomainModel = UserAdmissionDomainModel.of(
                "test",
                (UserDomainModel) this.mockUserDomainModel,
                '/test',
                'test',
                LocalDateTime.now(),
                LocalDateTime.now()
        )

        this.mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                (UserDomainModel) this.mockUserDomainModel
        )

        this.mockCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) mockCircleDomainModel,
                "test",
                "test",
                LocalDateTime.now(),
                LocalDateTime.now()
        )
    }

    /**
     * Test cases for user find by id
     */
    @Test
    def "User find by id normal case"() {
        given:
        def targetUserDomainModel = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById("test1") >> Optional.of(targetUserDomainModel)

        when: "request user's role is president"
        def userResponseDto = this.userService.findById("test1", "test")

        then:
        userResponseDto instanceof UserResponseDto
        with(userResponseDto) {
            getId() == "test1"
        }

        when: "request user's role is leader circle"
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        this.mockCircleMemberDomainModel.setUserId("test1")

        this.circlePort.findByLeaderId("test") >> Optional.of(mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test1", "test") >> Optional.of(mockCircleMemberDomainModel)

        userResponseDto = this.userService.findById("test1", "test")

        then:
        userResponseDto instanceof UserResponseDto
        with(userResponseDto) {
            getId() == "test1"
        }
    }

    @Test
    def "User find by id unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)

        when:
        mockUserDomainModel.setRole(Role.COMMON)
        this.userService.findById("test1", "test")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for user find post
     */
    @Test
    def "User find post normal case"() {
        given:
        def mockBoardDomainModel = BoardDomainModel.of(
                "test board id",
                "test board name",
                "test board description",
                Arrays.asList("PRESIDENT"),
                "category",
                false,
                null
        )

        def mockPostDomainModel = PostDomainModel.of(
                "test post id",
                "test post title",
                "test post content",
                (UserDomainModel) this.mockUserDomainModel,
                false,
                mockBoardDomainModel,
                null,
                null,
                List.of()
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.postPort.findByUserId(((UserDomainModel) this.mockUserDomainModel).getId(), 0) >> new PageImpl<PostDomainModel>(List.of(mockPostDomainModel))

        when:
        def userPostResponseDto = this.userService.findPosts("test", 0)

        then:
        userPostResponseDto instanceof UserPostsResponseDto
        with(userPostResponseDto) {
            getId() == "test"
            getPost().getContent().get(0).getId() == "test post id"
        }
    }

    /**
     * Test cases for user find comment
     */
    @Test
    def "User find comment normal case"() {
        given:
        def mockBoardDomainModel = BoardDomainModel.of(
                "test board id",
                "test board name",
                "test board description",
                Arrays.asList("PRESIDENT"),
                "category",
                false,
                null
        )

        def mockPostDomainModel = PostDomainModel.of(
                "test post id",
                "test post title",
                "test post content",
                (UserDomainModel) this.mockUserDomainModel,
                false,
                mockBoardDomainModel,
                null,
                null,
                List.of()
        )

        def mockCommentDomainModel = CommentDomainModel.of(
                "test comment id",
                "test comment content",
                false,
                null,
                null,
                (UserDomainModel) this.mockUserDomainModel,
                mockPostDomainModel.getId()
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.postPort.findById("test post id") >> Optional.of(mockPostDomainModel)
        this.commentPort.findByUserId(((UserDomainModel) this.mockUserDomainModel).getId(), 0) >> new PageImpl<CommentDomainModel>(List.of(mockCommentDomainModel))

        when:
        def userCommentResponseDto = this.userService.findComments("test", 0)

        then:
        userCommentResponseDto instanceof UserCommentsResponseDto
        with(userCommentResponseDto) {
            getId() == "test"
            getComment().getContent().get(0).getId() == "test comment id"
        }
    }

    /**
     * Test cases for user find by name
     */
    @Test
    def "User find by name normal case"() {
        given:
        def targetUserDomainModel = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test1",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findByName("test1") >> List.of(targetUserDomainModel)

        when: "request user's role is president"
        def userResponseDtoList = this.userService.findByName("test", "test1")

        then:
        userResponseDtoList instanceof List<UserResponseDto>
        with(userResponseDtoList) {
            get(0).getId() == "test1"
            get(0).getName() == "test1"
        }

        when: "request user's role is leader circle"
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        this.mockCircleMemberDomainModel.setUserId("test1")

        this.circlePort.findByLeaderId("test") >> Optional.of(mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test1", "test") >> Optional.of(mockCircleMemberDomainModel)

        userResponseDtoList = this.userService.findByName("test", "test1")

        then:
        userResponseDtoList instanceof List<UserResponseDto>
        with(userResponseDtoList) {
            get(0).getId() == "test1"
            get(0).getName() == "test1"
            get(0).getCircleIdIfLeader() == "test"
        }
    }

    @Test
    def "User find by name unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)

        when:
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userService.findByName("test", "test1")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for user find privileged users
     */
    @Test
    def "User find privileged users normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findByRole(Role.COUNCIL) >> List.of()
        this.userPort.findByRole(Role.LEADER_1) >> List.of()
        this.userPort.findByRole(Role.LEADER_2) >> List.of()
        this.userPort.findByRole(Role.LEADER_3) >> List.of()
        this.userPort.findByRole(Role.LEADER_4) >> List.of()
        this.userPort.findByRole(Role.LEADER_CIRCLE) >> List.of()
        this.userPort.findByRole(Role.LEADER_ALUMNI) >> List.of()

        when:
        def userPrivilegedResponseDto = this.userService.findPrivilegedUsers("test")

        then:
        userPrivilegedResponseDto instanceof UserPrivilegedResponseDto
    }

    @Test
    def "User find privileged users unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)

        when:
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userService.findPrivilegedUsers("test")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for user find by state
     */
    @Test
    def "User find by state normal case"() {
        given:
        def targetUserDomainModel = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findByState(UserState.ACTIVE, 0) >> new PageImpl<UserDomainModel>(List.of((UserDomainModel)targetUserDomainModel))

        when:
        def userResponseDto = this.userService.findByState("test", "ACTIVE", 0)

        then:
        userResponseDto instanceof Page<UserResponseDto>
        with(userResponseDto) {
            getContent().get(0).getId() == "test1"
        }
    }

    @Test
    def "User find by state unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)

        when:
        mockUserDomainModel.setRole(Role.COMMON)
        this.userService.findByState("test", "ACTIVE", 0)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for get circle list
     */
    @Test
    def "User get circle list normal case"() {
        given:
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        this.mockUserDomainModel.setState(UserState.ACTIVE)
        def circle = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                (UserDomainModel) this.mockUserDomainModel
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.circleMemberPort.getCircleListByUserId("test") >> List.of(circle)

        when: "request user's role is not admin"
        def circleResponseDtoList = this.userService.getCircleList("test")

        then:
        circleResponseDtoList instanceof List<CircleResponseDto>
        with(circleResponseDtoList) {
            get(0).getId() == "test"
            get(0).getName() == "test"
        }

        when: "request user's role is admin"
        this.mockUserDomainModel.setRole(Role.ADMIN)
        this.circlePort.findAll() >> List.of(circle)
        circleResponseDtoList = this.userService.getCircleList("test")

        then:
        circleResponseDtoList instanceof List<CircleResponseDto>
        with(circleResponseDtoList) {
            get(0).getId() == "test"
            get(0).getName() == "test"
        }
    }

    /**
     * Test cases for user sign-up
     */
    @Test
    def "User sign-up normal case"() {
        given:
        def userCreateRequestDto = new UserCreateRequestDto(
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                "/profile"
        )

        this.passwordEncoder.encode("test1234!") >> "test1234!"
        this.userPort.create((UserDomainModel) this.mockUserDomainModel) >> this.mockUserDomainModel
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)

        PowerMockito.mockStatic(UserDomainModel.class)
        PowerMockito.when(UserDomainModel.of(
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                "/profile",
        )).thenReturn((UserDomainModel) this.mockUserDomainModel)

        when:
        def userResponseDto = this.userService.signUp(userCreateRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        with(userResponseDto) {
            getEmail() == this.mockUserDomainModel.getEmail()
            getName() == this.mockUserDomainModel.getName()
            getStudentId() == this.mockUserDomainModel.getStudentId()
            getAdmissionYear() == this.mockUserDomainModel.getAdmissionYear()
            getRole() == this.mockUserDomainModel.getRole()
            getState() == this.mockUserDomainModel.getState()
        }
    }

    @Test
    def "User sign-up invalid password format case"() {
        given:
        def userCreateRequestDto = new UserCreateRequestDto(
                "test@cau.ac.kr",
                "test",
                "",
                "20210000",
                2021,
                "/profile"
        )

        def mockCreatedUserDomainModel = UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                userCreateRequestDto.getProfileImage(),
        )

        this.userPort.create(mockCreatedUserDomainModel) >> mockCreatedUserDomainModel
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)

        when: "password with short length"
        mockCreatedUserDomainModel.setPassword("test12!")
        userCreateRequestDto.setPassword("test12!")
        this.passwordEncoder.encode("test12!") >> "test12!"
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without special character"
        mockCreatedUserDomainModel.setPassword("test1234")
        userCreateRequestDto.setPassword("test1234")
        this.passwordEncoder.encode("test1234") >> "test1234"
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without number"
        mockCreatedUserDomainModel.setPassword("test!!!!")
        userCreateRequestDto.setPassword("test!!!!")
        this.passwordEncoder.encode("test!!!!") >> "test!!!!"
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without english"
        mockCreatedUserDomainModel.setPassword("1234567!")
        userCreateRequestDto.setPassword("1234567!")
        this.passwordEncoder.encode("1234567!") >> "1234567!"
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "User sign-up invalid admission year case"() {
        given:
        def userCreateRequestDto = new UserCreateRequestDto(
                "test@cau.ac.kr",
                "test",
                "test123!",
                "20210000",
                0,
                "/profile"
        )

        def mockCreatedUserDomainModel = UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                userCreateRequestDto.getProfileImage()
        )

        this.passwordEncoder.encode("test123!") >> "test123!"
        this.userPort.create(mockCreatedUserDomainModel) >> mockCreatedUserDomainModel
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)

        when: "admission year with future day"
        userCreateRequestDto.setAdmissionYear(2100)
        mockCreatedUserDomainModel.setAdmissionYear(2100)
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "admission year with past day"
        userCreateRequestDto.setAdmissionYear(1971)
        mockCreatedUserDomainModel.setAdmissionYear(1971)
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "User sign-up duplicate email case"() {
        given:
        def userCreateRequestDto = new UserCreateRequestDto(
                "test@cau.ac.kr",
                "test-name",
                "test1234!",
                "20210000",
                2021,
                "/profile"
        )

        def mockCreatedUserDomainModel = UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                userCreateRequestDto.getProfileImage(),
        )

        this.userPort.create(mockCreatedUserDomainModel) >> mockCreatedUserDomainModel
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.of(mockCreatedUserDomainModel)

        when: "findByEmail() returns object : expected fail"
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "User sign-up invalid parameter"() {
        given:
        def userCreateRequestDto = new UserCreateRequestDto(
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                "/profile"
        )

        def mockCreatedUserDomainModel = UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                userCreateRequestDto.getProfileImage()
        )

        this.passwordEncoder.encode("test1234!") >> "test1234!"
        this.userPort.create(mockCreatedUserDomainModel) >> mockCreatedUserDomainModel
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)
        this.userPort.findByEmail("invalid-email") >> Optional.ofNullable(null)
        this.userPort.findByEmail(null) >> Optional.ofNullable(null)

        PowerMockito.mockStatic(UserDomainModel.class)

        when: "Invalid email"
        userCreateRequestDto.setEmail("invalid-email")
        mockCreatedUserDomainModel.setEmail("invalid-email")

        PowerMockito.when(UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                userCreateRequestDto.getProfileImage(),
        )).thenReturn(mockCreatedUserDomainModel)

        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "Null email"
        userCreateRequestDto.setEmail(null)
        mockCreatedUserDomainModel.setEmail(null)

        PowerMockito.when(UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                userCreateRequestDto.getProfileImage(),
        )).thenReturn(mockCreatedUserDomainModel)

        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "Blank name"
        userCreateRequestDto.setEmail("test@cau.ac.kr")
        userCreateRequestDto.setName("")
        mockCreatedUserDomainModel.setEmail("test@cau.ac.kr")
        mockCreatedUserDomainModel.setName("")

        PowerMockito.when(UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                userCreateRequestDto.getProfileImage(),
        )).thenReturn(mockCreatedUserDomainModel)

        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "Null admission year"
        userCreateRequestDto.setName("test")
        userCreateRequestDto.setAdmissionYear(null)
        mockCreatedUserDomainModel.setName("test")
        mockCreatedUserDomainModel.setAdmissionYear(null)

        PowerMockito.when(UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                userCreateRequestDto.getProfileImage(),
        )).thenReturn(mockCreatedUserDomainModel)

        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    /**
     * Test cases for user sign-in
     */
    @Test
    def "User sign-in normal case"() {
        given:
        def userSignInRequestDto = new UserSignInRequestDto(
                "test@cau.ac.kr",
                "test1234!"
        )

        this.passwordEncoder.matches("test1234!", "test1234!") >> true
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(this.mockUserDomainModel)
        this.jwtTokenProvider.createToken("test", Role.PRESIDENT, UserState.ACTIVE) >> "token"

        when: "success sign-in"
        def token = this.userService.signIn(userSignInRequestDto)

        then:
        token instanceof String
        token == "token"
    }

    @Test
    def "User sign-in unauthorized case"() {
        given:
        def userSignInRequestDto = new UserSignInRequestDto(
                "test@cau.ac.kr",
                "test1234!"
        )

        this.passwordEncoder.matches("test1234!", "test1234!") >> true
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(this.mockUserDomainModel)

        when:
        this.mockUserDomainModel.setState(UserState.DROP)
        this.userService.signIn(userSignInRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "sign-in with await user"
        this.mockUserDomainModel.setState(UserState.AWAIT)
        this.userAdmissionPort.findByUserId("test") >> Optional.of(this.mockUserAdmissionDomainModel)
        this.userService.signIn(userSignInRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "User sign-in not found admission"() {
        given:
        def userSignInRequestDto = new UserSignInRequestDto(
                "test@cau.ac.kr",
                "test1234!"
        )

        this.passwordEncoder.matches("test1234!", "test1234!") >> true
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(this.mockUserDomainModel)

        when: "sign-in with await user"
        this.mockUserDomainModel.setState(UserState.AWAIT)
        this.userAdmissionPort.findByUserId("test") >> Optional.ofNullable(null)
        this.userService.signIn(userSignInRequestDto)

        then:
        thrown(BadRequestException)
    }

    /**
     * Test cases for user is duplicated email
     */
    @Test
    def "User is duplicated email normal case"() {
        given:
        this.userPort.findByEmail("test1@cau.ac.kr") >> Optional.ofNullable(null)

        when: "not found email"
        def duplicatedCheckResponseDto = this.userService.isDuplicatedEmail("test1@cau.ac.kr")

        then:
        duplicatedCheckResponseDto instanceof DuplicatedCheckResponseDto
        with (duplicatedCheckResponseDto) {
            !getResult()
        }

        when: "found email"
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(this.mockUserDomainModel)
        duplicatedCheckResponseDto = this.userService.isDuplicatedEmail("test@cau.ac.kr")

        then:
        duplicatedCheckResponseDto instanceof DuplicatedCheckResponseDto
        with (duplicatedCheckResponseDto) {
            getResult()
        }
    }

    @Test
    def "User is duplicated email with invalid state"() {
        given:
        this.userPort.findByEmail("test1@cau.ac.kr") >> Optional.ofNullable(this.mockUserDomainModel)

        when: "user is inactive"
        mockUserDomainModel.setState(UserState.INACTIVE)
        this.userService.isDuplicatedEmail("test1@cau.ac.kr")

        then:
        thrown(BadRequestException)

        when: "user is drop"
        mockUserDomainModel.setState(UserState.DROP)
        this.userService.isDuplicatedEmail("test1@cau.ac.kr")

        then:
        thrown(BadRequestException)
    }

    /**
     * Test cases for user update
     */
    @Test
    def "User update normal case"() {
        given:
        def id = "test"
        def name = "test"
        def studentId = "20210000"
        def admissionYear = 2021

        def userUpdateRequestDto = new UserUpdateRequestDto(
                "update@cau.ac.kr",
                name,
                studentId,
                admissionYear,
                "/profile"
        )

        def mockUpdatedUserDomainModel = UserDomainModel.of(
                id,
                userUpdateRequestDto.getEmail(),
                userUpdateRequestDto.getName(),
                (String) this.mockUserDomainModel.getPassword(),
                userUpdateRequestDto.getStudentId(),
                userUpdateRequestDto.getAdmissionYear(),
                (Role) this.mockUserDomainModel.getRole(),
                userUpdateRequestDto.getProfileImage(),
                (UserState) this.mockUserDomainModel.getState()
        )

        this.userPort.findById(id) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findByEmail("update@cau.ac.kr") >> Optional.ofNullable(null)

        this.userPort.update(id, (UserDomainModel) this.mockUserDomainModel) >> Optional.of(mockUpdatedUserDomainModel)

        when:
        def userResponseDto = this.userService.update(id, userUpdateRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getEmail() == "update@cau.ac.kr"
    }

    @Test
    def "User update invalid admission year case"() {
        given:
        def id = "test"
        def email = "test@cau.ac.kr"
        def name = "test-name"
        def studentId = "20210000"

        def userUpdateRequestDto = new UserUpdateRequestDto(
                email,
                name,
                studentId,
                2021,
                "/profile"
        )

        this.userPort.findById(id) >> Optional.of(this.mockUserDomainModel)

        when: "admission year with future day"
        userUpdateRequestDto.setAdmissionYear(2100)
        this.userService.update(id, userUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "admission year with past day"
        userUpdateRequestDto.setAdmissionYear(1971)
        this.userService.update(id, userUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    /**
     * Test cases for user role update
     */
    @Test
    def "User role grant normal case"() {
        given:
        def currentId = "test"
        def targetId = "test1"

        def mockTargetUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.NONE,
                null,
                UserState.AWAIT
        )

        def mockUpdatedUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.AWAIT
        )

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                (UserDomainModel) this.mockUserDomainModel
        )

        def mockCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.MEMBER,
                mockCircleDomainModel,
                targetId,
                "test",
                LocalDateTime.now(),
                LocalDateTime.now()
        )

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COUNCIL.value, null)

        this.userPort.findById(currentId) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserDomainModel)

        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserDomainModel)

        when: "President -> Grant Council"
        mockUpdatedUserDomainModel.setRole(Role.COUNCIL)
        userUpdateRoleRequestDto.setRole(Role.COUNCIL.value)
        def userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.COUNCIL

        when: "President -> Grant Leader_1"
        mockUpdatedUserDomainModel.setRole(Role.LEADER_1)
        userUpdateRoleRequestDto.setRole(Role.LEADER_1.value)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_1


        when: "President -> Grant Leader_Alumni"
        mockUpdatedUserDomainModel.setRole(Role.LEADER_ALUMNI)
        userUpdateRoleRequestDto.setRole(Role.LEADER_ALUMNI.value)
        this.userPort.findByRole(Role.LEADER_ALUMNI) >> List.of(this.mockUserDomainModel)
        this.userPort.updateRole(((UserDomainModel) this.mockUserDomainModel).getId(), Role.COMMON) >> Optional.of(this.mockUserDomainModel)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_ALUMNI

        when: "President -> Grant Leader_Circle"
        userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COUNCIL.value, "test")

        this.circlePort.findById("test") >> Optional.of(mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId(targetId, "test") >> Optional.of(mockCircleMemberDomainModel)
        this.circlePort.updateLeader("test", mockTargetUserDomainModel) >> Optional.of(mockCircleDomainModel)

        mockUpdatedUserDomainModel.setRole(Role.LEADER_CIRCLE)
        userUpdateRoleRequestDto.setRole(Role.LEADER_CIRCLE.value)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_CIRCLE
    }

    @Test
    def "User role delegate normal case"() {
        given:
        def currentId = "test"
        def targetId = "test1"

        def mockTargetUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.AWAIT
        )

        def mockUpdatedUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.AWAIT
        )

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                (UserDomainModel) this.mockUserDomainModel
        )

        def mockCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.MEMBER,
                mockCircleDomainModel,
                "test1",
                "test",
                null,
                null
        )

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COUNCIL.value, null)

        this.userPort.findById(currentId) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserDomainModel)
        this.circlePort.findByLeaderId(currentId) >> Optional.of(mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId(targetId, currentId) >> Optional.of(mockCircleMemberDomainModel)

        this.userPort.updateRole(targetId, Role.PRESIDENT) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(currentId, Role.COMMON) >> Optional.of(mockUpdatedUserDomainModel)
        this.circlePort.updateLeader(currentId, mockTargetUserDomainModel) >> Optional.of(mockCircleDomainModel)

        when: "Leader Alumni -> Delegate Alumni"
        this.mockUserDomainModel.setRole(Role.LEADER_ALUMNI)
        userUpdateRoleRequestDto.setRole(Role.LEADER_ALUMNI.value)
        mockUpdatedUserDomainModel.setRole(Role.LEADER_ALUMNI)
        def userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_ALUMNI

        when: "Leader Circle -> Delegate Circle"
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        userUpdateRoleRequestDto.setRole(Role.LEADER_CIRCLE.value)
        mockUpdatedUserDomainModel.setRole(Role.LEADER_CIRCLE)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_CIRCLE

        when: "President -> Delegate President"
        this.mockUserDomainModel.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.PRESIDENT.value)
        mockUpdatedUserDomainModel.setRole(Role.PRESIDENT)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.PRESIDENT
    }

    @Test
    def "User role update invalid grantee"() {
        given:
        def currentId = "test"
        def targetId = "test1"

        def mockTargetUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.NONE,
                null,
                UserState.AWAIT
        )

        def mockUpdatedUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.AWAIT
        )

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COMMON.value, null)

        this.userPort.findById(currentId) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserDomainModel)

        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserDomainModel)

        when: "Admin -> Grant something to Admin user"
        mockUpdatedUserDomainModel.setRole(Role.ADMIN)
        mockTargetUserDomainModel.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant something to President user"
        mockUpdatedUserDomainModel.setRole(Role.PRESIDENT)
        mockTargetUserDomainModel.setRole(Role.PRESIDENT)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant something to Admin user"
        mockUpdatedUserDomainModel.setRole(Role.PRESIDENT)
        mockTargetUserDomainModel.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "User role update invalid grantor"() {
        given:
        def currentId = "test"
        def targetId = "test1"

        def mockTargetUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.NONE,
                null,
                UserState.AWAIT
        )

        def mockUpdatedUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.AWAIT
        )

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COMMON.value, null)

        this.mockUserDomainModel.setRole(Role.NONE)
        this.userPort.findById(currentId) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserDomainModel)

        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserDomainModel)

        when: "Admin -> Grant Admin"
        mockUpdatedUserDomainModel.setRole(Role.ADMIN)
        userUpdateRoleRequestDto.setRole(Role.ADMIN.value)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant Admin"
        mockUpdatedUserDomainModel.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.ADMIN.value)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant Admin"
        mockUpdatedUserDomainModel.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.ADMIN.value)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Leader Circle -> Grant Common"
        mockUpdatedUserDomainModel.setRole(Role.LEADER_CIRCLE)
        userUpdateRoleRequestDto.setRole(Role.COMMON.value)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Leader Alumni -> Grant Common"
        mockUpdatedUserDomainModel.setRole(Role.LEADER_ALUMNI)
        userUpdateRoleRequestDto.setRole(Role.COMMON.value)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for user password update
     */
    @Test
    def "User password update normal case"() {
        given:
        def userPasswordUpdateRequestDto = new UserUpdatePasswordRequestDto(
                "test1234!",
                "test12345!"
        )

        def mockUpdatedUserDomainModel = UserDomainModel.of(
                (String) this.mockUserDomainModel.getId(),
                (String) this.mockUserDomainModel.getEmail(),
                (String) this.mockUserDomainModel.getName(),
                "test12345!",
                (String) this.mockUserDomainModel.getStudentId(),
                (Integer) this.mockUserDomainModel.getAdmissionYear(),
                Role.PRESIDENT,
                null,
                UserState.AWAIT
        )

        this.passwordEncoder.matches("test1234!", "test1234!") >> true
        this.passwordEncoder.encode("test12345!") >> "test12345!"
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.updatePassword("test", "test12345!") >> Optional.of(mockUpdatedUserDomainModel)

        when:
        def userResponseDto = this.userService.updatePassword("test", userPasswordUpdateRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
    }

    @Test
    def "User password update invalid origin password"() {
        given:
        def userPasswordUpdateRequestDto = new UserUpdatePasswordRequestDto(
                "test12345!",
                "test12345!"
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)

        when:
        this.userService.updatePassword("test", userPasswordUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "User password update invalid password format"() {
        given:
        def userPasswordUpdateRequestDto = new UserUpdatePasswordRequestDto(
                "test1234!",
                "test12345!"
        )

        this.passwordEncoder.matches("test1234!", "test1234!") >> true
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)

        when: "password with short length"
        userPasswordUpdateRequestDto.setUpdatedPassword("test12!")
        this.userService.updatePassword("test", userPasswordUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without special character"
        userPasswordUpdateRequestDto.setUpdatedPassword("test1234")
        this.userService.updatePassword("test", userPasswordUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without number"
        userPasswordUpdateRequestDto.setUpdatedPassword("test!!!!")
        this.userService.updatePassword("test", userPasswordUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without english"
        userPasswordUpdateRequestDto.setUpdatedPassword("1234567!")
        this.userService.updatePassword("test", userPasswordUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    /**
     * Test cases for user leave
     */
    @Test
    def "User leave normal case"() {
        given:
        this.mockUserDomainModel.setRole(Role.COMMON)

        def leavedUser = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.NONE,
                null,
                UserState.INACTIVE
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.lockerPort.findByUserId("test") >> Optional.empty()
        this.userPort.updateRole("test", Role.NONE) >> Optional.of(leavedUser)
        this.circleMemberPort.findByUserId("test") >> List.of(this.mockCircleMemberDomainModel)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.LEAVE) >> Optional.of(this.mockCircleMemberDomainModel)
        this.userPort.updateState("test", UserState.INACTIVE) >> Optional.of(leavedUser)

        when:
        def userResponseDto = this.userService.leave("test")

        then:
        userResponseDto instanceof UserResponseDto
        with(userResponseDto) {
            getState() == UserState.INACTIVE
        }
    }

    @Test
    def "User leave invalid user role"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)

        when: "User Role is president"
        this.mockUserDomainModel.setRole(Role.PRESIDENT)
        this.userService.leave("test")

        then:
        thrown(UnauthorizedException)

        when: "User Role is leader of circle"
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        this.userService.leave("test")

        then:
        thrown(UnauthorizedException)

        when: "User Role is leader of alumni"
        this.mockUserDomainModel.setRole(Role.LEADER_ALUMNI)
        this.userService.leave("test")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for drop user
     */
    @Test
    def "User drop normal case"() {
        given:
        def mockDroppedUserDomainModel = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.DROP
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById("test1") >> Optional.of(mockDroppedUserDomainModel)
        this.userPort.updateRole("test1", Role.NONE) >> Optional.of(mockDroppedUserDomainModel)
        this.userPort.updateState("test1", UserState.DROP) >> Optional.of(mockDroppedUserDomainModel)
        this.lockerPort.findByUserId("test1") >> Optional.empty()

        when:
        def userResponseDto = this.userService.dropUser("test", "test1")

        then:
        userResponseDto instanceof UserResponseDto
        with(userResponseDto) {
            getId() == "test1"
            getState() == UserState.DROP
        }
    }

    @Test
    def "User drop invalid api call user role"() {
        given:
        def mockDroppedUserDomainModel = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.DROP
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById("test1") >> Optional.of(mockDroppedUserDomainModel)

        when:
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userService.dropUser("test", "test1")

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "User drop invalid dropped user role"() {
        given:
        def mockDroppedUserDomainModel = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COUNCIL,
                null,
                UserState.DROP
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById("test1") >> Optional.of(mockDroppedUserDomainModel)

        when:
        this.userService.dropUser("test", "test1")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for user find admission
     */
    @Test
    def "User find admission normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)

        this.userAdmissionPort.findById("test") >> Optional.of(this.mockUserAdmissionDomainModel)

        when:
        def userAdmissionResponseDto = this.userService.findAdmissionById("test", "test")

        then:
        userAdmissionResponseDto instanceof UserAdmissionResponseDto
        with(userAdmissionResponseDto) {
            getUser().getId() == ((UserDomainModel) this.mockUserDomainModel).getId()
        }
    }

    @Test
    def "User find admission unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)

        when:
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userService.findAdmissionById("test", "test")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for user find admission all
     */
    @Test
    def "User find admission all normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)

        this.userAdmissionPort.findAll(UserState.AWAIT, 0) >> new PageImpl<UserAdmissionDomainModel>(List.of((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel))

        when:
        def userAdmissionResponseDto = this.userService.findAllAdmissions("test", 0)

        then:
        userAdmissionResponseDto instanceof Page<UserAdmissionsResponseDto>
        with(userAdmissionResponseDto) {
            getContent().get(0).getUserName() == ((UserDomainModel) this.mockUserDomainModel).getName()
        }
    }

    @Test
    def "User find admission all unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)

        when:
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userService.findAllAdmissions("test", 0)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for user create admission
     */
    @Test
    def "User create admission normal case"() {
        given:
        def userAdmissionCreateRequestDto = new UserAdmissionCreateRequestDto(
                ((UserDomainModel) this.mockUserDomainModel).getEmail(),
                "",
                null
        )

        def createdUserAdmissionDomainModel = UserAdmissionDomainModel.of(
                (UserDomainModel) this.mockUserDomainModel,
                null,
                userAdmissionCreateRequestDto.getDescription()
        )

        this.mockUserDomainModel.setState(UserState.AWAIT)
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.existsByUserId(((UserDomainModel) this.mockUserDomainModel).getId()) >> false

        PowerMockito.mockStatic(UserAdmissionDomainModel.class)
        PowerMockito.when(UserAdmissionDomainModel.of(
                (UserDomainModel) this.mockUserDomainModel,
                null,
                userAdmissionCreateRequestDto.getDescription()
        )).thenReturn(createdUserAdmissionDomainModel)

        this.userAdmissionPort.create(createdUserAdmissionDomainModel) >> createdUserAdmissionDomainModel

        when:
        def userAdmissionResponseDto = this.userService.createAdmission(userAdmissionCreateRequestDto)

        then:
        userAdmissionResponseDto instanceof UserAdmissionResponseDto
        with(userAdmissionResponseDto) {
            getUser().getId() == ((UserDomainModel) this.mockUserDomainModel).getId()
        }
    }

    @Test
    def "User create admission unauthorized case"() {
        given:
        def userAdmissionCreateRequestDto = new UserAdmissionCreateRequestDto(
                ((UserDomainModel) this.mockUserDomainModel).getEmail(),
                "",
                null
        )

        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.existsByUserId(((UserDomainModel) this.mockUserDomainModel).getId()) >> false

        when: "User is dropped"
        this.mockUserDomainModel.setState(UserState.DROP)
        this.userService.createAdmission(userAdmissionCreateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "User is already active"
        this.mockUserDomainModel.setState(UserState.ACTIVE)
        this.userService.createAdmission(userAdmissionCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "User create admission invalid format"() {
        given:
        def description = ""
        for (int i = 0; i < 300; i++) {
            description += "r"
        }

        def userAdmissionCreateRequestDto = new UserAdmissionCreateRequestDto(
                ((UserDomainModel) this.mockUserDomainModel).getEmail(),
                description,
                null
        )

        this.mockUserDomainModel.setState(UserState.AWAIT)
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.existsByUserId(((UserDomainModel) this.mockUserDomainModel).getId()) >> false

        when:
        this.userService.createAdmission(userAdmissionCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    /**
     * Test cases for user admission accept
     */
    @Test
    def "User accept admission normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.findById("test") >> Optional.of(this.mockUserAdmissionDomainModel)
        this.userPort.updateRole("test", Role.COMMON) >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionLogPort.create(
                ((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel).getUser().getEmail(),
                ((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel).getUser().getName(),
                ((UserDomainModel)this.mockUserDomainModel).getEmail(),
                ((UserDomainModel)this.mockUserDomainModel).getName(),
                UserAdmissionLogAction.ACCEPT,
                ((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel).getAttachImage(),
                ((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel).getDescription()
        ) >> null
        this.userAdmissionPort.delete((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel) >> null
        this.userPort.updateState(((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel).getUser().getId(), UserState.ACTIVE) >> Optional.of(this.mockUserDomainModel)

        when:
        def userAdmissionResponseDto = this.userService.accept("test", "test")

        then:
        userAdmissionResponseDto instanceof UserAdmissionResponseDto
        with(userAdmissionResponseDto) {
            getUser().getId() == this.mockUserDomainModel.getId()
        }
    }

    @Test
    def "User accept admission unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.findById("test") >> Optional.of(this.mockUserAdmissionDomainModel)

        when: "request user is not president and admin"
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userService.accept("test", "test")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for user admission reject
     */
    @Test
    def "User reject admission normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.findById("test") >> Optional.of(this.mockUserAdmissionDomainModel)
        this.userPort.updateRole("test", Role.COMMON) >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionLogPort.create(
                ((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel).getUser().getEmail(),
                ((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel).getUser().getName(),
                ((UserDomainModel)this.mockUserDomainModel).getEmail(),
                ((UserDomainModel)this.mockUserDomainModel).getName(),
                UserAdmissionLogAction.REJECT,
                ((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel).getAttachImage(),
                ((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel).getDescription()
        ) >> null
        this.userAdmissionPort.delete((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel) >> null

        this.userPort.updateState(((UserAdmissionDomainModel)this.mockUserAdmissionDomainModel).getUser().getId(), UserState.REJECT) >> Optional.of(this.mockUserDomainModel)

        when:
        def userAdmissionResponseDto = this.userService.reject("test", "test")

        then:
        userAdmissionResponseDto instanceof UserAdmissionResponseDto
        with(userAdmissionResponseDto) {
            getUser().getId() == this.mockUserDomainModel.getId()
        }
    }

    @Test
    def "User reject admission unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.findById("test") >> Optional.of(this.mockUserAdmissionDomainModel)

        when: "request user is not president and admin"
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userService.reject("test", "test")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for create favorite board
     */
    // TODO: delete comment
//    @Test
//    def "User create favorite board normal case"() {
//        given:
//        def mockBoardDomainModel = BoardDomainModel.of(
//                "test board id",
//                "test board name",
//                "test board description",
//                Arrays.asList("PRESIDENT"),
//                "category",
//                false,
//                null
//        )
//
//        def mockFavoriteBoardDomainModel = FavoriteBoardDomainModel.of(
//                (UserDomainModel) this.mockUserDomainModel,
//                mockBoardDomainModel
//        )
//
//        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
//        this.boardPort.findById("test board id") >> Optional.of(mockBoardDomainModel)
//
//        this.favoriteBoardPort.create(mockFavoriteBoardDomainModel) >> mockFavoriteBoardDomainModel
//
//        PowerMockito.mockStatic(FavoriteBoardDomainModel.class)
//
//        when: "create favorite board without circle"
//        PowerMockito.when(FavoriteBoardDomainModel.of(
//                (UserDomainModel) this.mockUserDomainModel,
//                mockBoardDomainModel
//        )).thenReturn((FavoriteBoardDomainModel) mockFavoriteBoardDomainModel)
//
//        def boardResponseDto = this.userService.createFavoriteBoard("test", "test board id")
//
//        then:
//        boardResponseDto instanceof BoardResponseDto
//        with (boardResponseDto) {
//            getId() == "test board id"
//        }
//
//        when: "create favorite board with circle"
//        mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
//        PowerMockito.when(FavoriteBoardDomainModel.of(
//                (UserDomainModel) this.mockUserDomainModel,
//                mockBoardDomainModel
//        )).thenReturn((FavoriteBoardDomainModel) mockFavoriteBoardDomainModel)
//        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)
//
//        boardResponseDto = this.userService.createFavoriteBoard("test", "test board id")
//
//        then:
//        boardResponseDto instanceof BoardResponseDto
//        with (boardResponseDto) {
//            getId() == "test board id"
//        }
//
//        when: "create favorite board for admin"
//        this.mockUserDomainModel.setRole(Role.ADMIN)
//
//        boardResponseDto = this.userService.createFavoriteBoard("test", "test board id")
//
//        then:
//        boardResponseDto instanceof BoardResponseDto
//        with (boardResponseDto) {
//            getId() == "test board id"
//        }
//    }
//
//    @Test
//    def "User create favorite board deleted case"() {
//        given:
//        def mockBoardDomainModel = BoardDomainModel.of(
//                "test board id",
//                "test board name",
//                "test board description",
//                Arrays.asList("PRESIDENT"),
//                "category",
//                true,
//                null
//        )
//
//        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
//        this.boardPort.findById("test board id") >> Optional.of(mockBoardDomainModel)
//
//        when: "board is deleted"
//        this.userService.createFavoriteBoard("test", "test board id")
//
//        then:
//        thrown(BadRequestException)
//
//        when: "circle is deleted"
//        mockBoardDomainModel.setIsDeleted(false)
//        this.mockCircleDomainModel.setIsDeleted(true)
//        mockBoardDomainModel.setCircle((CircleDomainModel) this.mockCircleDomainModel)
//        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)
//
//        this.userService.createFavoriteBoard("test", "test board id")
//
//        then:
//        thrown(BadRequestException)
//    }
//
//    @Test
//    def "User create favorite board unauthorized case"() {
//        given:
//        def mockBoardDomainModel = BoardDomainModel.of(
//                "test board id",
//                "test board name",
//                "test board description",
//                Arrays.asList("PRESIDENT"),
//                "category",
//                false,
//                (CircleDomainModel) this.mockCircleDomainModel
//        )
//
//        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
//        this.boardPort.findById("test board id") >> Optional.of(mockBoardDomainModel)
//
//        when: "Bad request exception"
//        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
//        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)
//
//        this.userService.createFavoriteBoard("test", "test board id")
//
//        then:
//        thrown(BadRequestException)
//
//        when: "Unauthorized exception"
//        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
//        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)
//
//        this.userService.createFavoriteBoard("test", "test board id")
//
//        then:
//        thrown(UnauthorizedException)
//    }

    /**
     * Test cases for restore user
     */
    @Test
    def "User restore normal case"() {
        given:
        def mockDroppedUserDomainModel = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.NONE,
                null,
                UserState.DROP
        )

        def mockRestoredUserDomainModel = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById("test1") >> Optional.of(mockDroppedUserDomainModel)

        this.userPort.updateRole("test1", Role.COMMON) >> Optional.of(mockRestoredUserDomainModel)
        this.userPort.updateState("test1", UserState.ACTIVE) >> Optional.of(mockRestoredUserDomainModel)

        when:
        def userResponseDto = this.userService.restore("test", "test1")

        then:
        userResponseDto instanceof UserResponseDto
        with(userResponseDto) {
            getId() == "test1"
            getState() == UserState.ACTIVE
        }
    }

    @Test
    def "User restore unauthorized case"() {
        given:
        def userDomainModel = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.NONE,
                null,
                UserState.DROP
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById("test1") >> Optional.of(userDomainModel)

        when: "User is not dropped"
        userDomainModel.setRole(Role.COMMON)
        userDomainModel.setState(UserState.ACTIVE)
        this.userService.restore("test", "test1")

        then:
        thrown(UnauthorizedException)

        when: "Request user is not president or admin"
        ((UserDomainModel) this.mockUserDomainModel).setRole(Role.COMMON)
        this.userService.restore("test", "test1")

        then:
        thrown(UnauthorizedException)
    }
}