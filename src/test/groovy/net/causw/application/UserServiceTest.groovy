package net.causw.application

import net.causw.application.dto.*
import net.causw.application.spi.BoardPort
import net.causw.application.spi.CircleMemberPort
import net.causw.application.spi.CirclePort
import net.causw.application.spi.FavoriteBoardPort
import net.causw.application.spi.UserAdmissionLogPort
import net.causw.application.spi.UserAdmissionPort
import net.causw.application.spi.UserPort
import net.causw.config.JwtTokenProvider
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.*
import net.causw.infrastructure.GoogleMailSender
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.Validation
import javax.validation.Validator
import java.time.LocalDateTime

@ActiveProfiles(value = "test")
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest([UserDomainModel.class, UserAdmissionDomainModel.class])
class UserServiceTest extends Specification {
    private UserPort userPort = Mock(UserPort.class)
    private BoardPort boardPort = Mock(BoardPort.class)
    private UserAdmissionPort userAdmissionPort = Mock(UserAdmissionPort.class)
    private UserAdmissionLogPort userAdmissionLogPort = Mock(UserAdmissionLogPort.class)
    private CirclePort circlePort = Mock(CirclePort.class)
    private CircleMemberPort circleMemberPort = Mock(CircleMemberPort.class)
    private FavoriteBoardPort favoriteBoardPort = Mock(FavoriteBoardPort.class)
    private JwtTokenProvider jwtTokenProvider = Mock(JwtTokenProvider.class)
    private GoogleMailSender googleMailSender = Mock(GoogleMailSender.class)
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private UserService userService = new UserService(
            this.userPort,
            this.boardPort,
            this.userAdmissionPort,
            this.userAdmissionLogPort,
            this.circlePort,
            this.circleMemberPort,
            this.favoriteBoardPort,
            this.jwtTokenProvider,
            this.googleMailSender,
            this.validator
    )

    def mockUserDomainModel
    def mockUserAdmissionDomainModel

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

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COUNCIL)

        this.userPort.findById(currentId) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserDomainModel)

        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserDomainModel)

        when: "President -> Grant Council"
        mockUpdatedUserDomainModel.setRole(Role.COUNCIL)
        userUpdateRoleRequestDto.setRole(Role.COUNCIL)
        def userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.COUNCIL

        when: "President -> Grant Leader_1"
        mockUpdatedUserDomainModel.setRole(Role.LEADER_1)
        userUpdateRoleRequestDto.setRole(Role.LEADER_1)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_1

        when: "President -> Grant Leader_Circle"
        mockUpdatedUserDomainModel.setRole(Role.LEADER_CIRCLE)
        userUpdateRoleRequestDto.setRole(Role.LEADER_CIRCLE)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_CIRCLE

        when: "President -> Grant Leader_Alumni"
        mockUpdatedUserDomainModel.setRole(Role.LEADER_ALUMNI)
        userUpdateRoleRequestDto.setRole(Role.LEADER_ALUMNI)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_ALUMNI
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

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COUNCIL)

        this.userPort.findById(currentId) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserDomainModel)
        this.circlePort.findByLeaderId(currentId) >> Optional.of(mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId(targetId, currentId) >> Optional.of(mockCircleMemberDomainModel)

        this.userPort.updateRole(targetId, Role.PRESIDENT) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(currentId, Role.COMMON) >> Optional.of(mockUpdatedUserDomainModel)
        this.circlePort.updateLeader(currentId, mockTargetUserDomainModel) >> Optional.of(mockCircleDomainModel)

        when: "President -> Delegate President"
        this.mockUserDomainModel.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.PRESIDENT)
        mockUpdatedUserDomainModel.setRole(Role.PRESIDENT)
        def userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.PRESIDENT

        when: "Leader Alumni -> Delegate Alumni"
        this.mockUserDomainModel.setRole(Role.LEADER_ALUMNI)
        userUpdateRoleRequestDto.setRole(Role.LEADER_ALUMNI)
        mockUpdatedUserDomainModel.setRole(Role.LEADER_ALUMNI)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_ALUMNI

        when: "Leader Circle -> Delegate Circle"
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        userUpdateRoleRequestDto.setRole(Role.LEADER_CIRCLE)
        mockUpdatedUserDomainModel.setRole(Role.LEADER_CIRCLE)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_CIRCLE
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

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COMMON)

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

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COMMON)

        this.mockUserDomainModel.setRole(Role.NONE)
        this.userPort.findById(currentId) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserDomainModel)

        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserDomainModel)

        when: "Admin -> Grant Admin"
        mockUpdatedUserDomainModel.setRole(Role.ADMIN)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant Admin"
        mockUpdatedUserDomainModel.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant Admin"
        mockUpdatedUserDomainModel.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Leader Circle -> Grant Common"
        mockUpdatedUserDomainModel.setRole(Role.LEADER_CIRCLE)
        userUpdateRoleRequestDto.setRole(Role.COMMON)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Leader Alumni -> Grant Common"
        mockUpdatedUserDomainModel.setRole(Role.LEADER_ALUMNI)
        userUpdateRoleRequestDto.setRole(Role.COMMON)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)
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

        this.userPort.update(id, (UserDomainModel)this.mockUserDomainModel) >> Optional.of(mockUpdatedUserDomainModel)

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

        PowerMockito.mockStatic(UserDomainModel.class)
        PowerMockito.when(UserDomainModel.of(
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                "/profile",
        )).thenReturn((UserDomainModel) this.mockUserDomainModel)

        this.userPort.create((UserDomainModel) this.mockUserDomainModel) >> this.mockUserDomainModel
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)

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
    def "User sign-up invalid password case"() {
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
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without special character"
        mockCreatedUserDomainModel.setPassword("test1234")
        userCreateRequestDto.setPassword("test1234")
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without number"
        mockCreatedUserDomainModel.setPassword("test!!!!")
        userCreateRequestDto.setPassword("test!!!!")
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without english"
        mockCreatedUserDomainModel.setPassword("1234567!")
        userCreateRequestDto.setPassword("1234567!")
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

        this.userPort.create(mockCreatedUserDomainModel) >> mockCreatedUserDomainModel
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)
        this.userPort.findByEmail("invalid-email") >> Optional.ofNullable(null)
        this.userPort.findByEmail(null) >> Optional.ofNullable(null)

        when: "Invalid email"
        userCreateRequestDto.setEmail("invalid-email")
        mockCreatedUserDomainModel.setEmail("invalid-email")
        PowerMockito.mockStatic(UserDomainModel.class)
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
        PowerMockito.mockStatic(UserDomainModel.class)
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
        PowerMockito.mockStatic(UserDomainModel.class)
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
        PowerMockito.mockStatic(UserDomainModel.class)
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
     * Test cases for user password update
     */
    @Test
    def "User password update normal case"() {
        given:
        def userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto(
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
        def userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto(
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
        def userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto(
                "test1234!",
                "test12345!"
        )

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
        this.mockUserDomainModel.setState(UserState.ACTIVE)

        def mockUpdatedUserDomainModel = UserDomainModel.of(
                (String) this.mockUserDomainModel.getId(),
                (String) this.mockUserDomainModel.getEmail(),
                (String) this.mockUserDomainModel.getName(),
                "test12345!",
                (String) this.mockUserDomainModel.getStudentId(),
                (Integer) this.mockUserDomainModel.getAdmissionYear(),
                Role.NONE,
                null,
                UserState.INACTIVE
        )

        def leader = UserDomainModel.of(
                "test1",
                "test1@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.LEADER_CIRCLE,
                null,
                UserState.ACTIVE
        )

        def circle = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                leader
        )

        def circleMember = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.MEMBER,
                circle,
                "test",
                "test",
                null,
                null
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.updateRole("test", Role.NONE) >> Optional.of(mockUpdatedUserDomainModel)
        this.circleMemberPort.findByUserId("test") >> List.of(circleMember)
        circleMember.setStatus(CircleMemberStatus.LEAVE)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.LEAVE) >> Optional.of(circleMember)
        this.userPort.updateState("test", UserState.INACTIVE) >> Optional.of(mockUpdatedUserDomainModel)

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
        this.mockUserDomainModel.setState(UserState.ACTIVE)

        def mockUpdatedUserDomainModel = UserDomainModel.of(
                (String) this.mockUserDomainModel.getId(),
                (String) this.mockUserDomainModel.getEmail(),
                (String) this.mockUserDomainModel.getName(),
                "test12345!",
                (String) this.mockUserDomainModel.getStudentId(),
                (Integer) this.mockUserDomainModel.getAdmissionYear(),
                Role.PRESIDENT,
                null,
                UserState.INACTIVE
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.updateState("test", UserState.INACTIVE) >> Optional.of(mockUpdatedUserDomainModel)

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

        when:
        def circleResponseDtoList = this.userService.getCircleList("test")

        then:
        circleResponseDtoList instanceof List<CircleResponseDto>
        with(circleResponseDtoList) {
            get(0).getId() == "test"
            get(0).getName() == "test"
        }
    }

    /**
     * Test cases for find by name
     */
    @Test
    def "User find by name normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findByName("test") >> List.of(this.mockUserDomainModel)

        when:
        def userResponseDtoList = this.userService.findByName("test", "test")

        then:
        userResponseDtoList instanceof List<UserResponseDto>
        with(userResponseDtoList) {
            get(0).getId() == "test"
            get(0).getName() == "test"
        }
    }

    @Test
    def "User find by name invalid api call user role"() {
        given:
        def mockApiCallUser = UserDomainModel.of(
                "test1",
                "test1@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.AWAIT
        )

        this.userPort.findById("test1") >> Optional.of(mockApiCallUser)
        this.userPort.findByName("test") >> List.of((UserDomainModel) this.mockUserDomainModel, mockApiCallUser)

        when:
        this.userService.findByName("test1", "test")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for find by role
     */
    @Test
    def "User find by role normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userPort.findByRole(Role.PRESIDENT) >> List.of(this.mockUserDomainModel)

        when:
        def userResponseDtoList = this.userService.findByRole("test", Role.PRESIDENT)

        then:
        userResponseDtoList instanceof List<UserResponseDto>
        with(userResponseDtoList) {
            get(0).getId() == "test"
            get(0).getName() == "test"
        }
    }

    @Test
    def "User find by role invalid api call user role"() {
        given:
        def mockApiCallUser = UserDomainModel.of(
                "test1",
                "test1@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.AWAIT
        )

        this.userPort.findById("test1") >> Optional.of(mockApiCallUser)
        this.userPort.findByRole(Role.PRESIDENT) >> List.of((UserDomainModel) this.mockUserDomainModel, mockApiCallUser)

        when:
        this.userService.findByRole("test1", Role.PRESIDENT)

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

        when:
        this.mockUserDomainModel.setRole(Role.PRESIDENT)
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
                Role.PROFESSOR,
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
        ((UserDomainModel)this.mockUserDomainModel).setRole(Role.COMMON)
        this.userService.restore("test", "test1")

        then:
        thrown(UnauthorizedException)
    }


    /**
     * Test cases for user admission
     */
    @Test
    def "User create admission normal case"() {
        given:
        def userAdmissionCreateRequestDto = new UserAdmissionCreateRequestDto(
                ((UserDomainModel)this.mockUserDomainModel).getEmail(),
                "",
                ""
        )

        def createdUserAdmissionDomainModel = UserAdmissionDomainModel.of(
                (UserDomainModel)this.mockUserDomainModel,
                userAdmissionCreateRequestDto.getAttachImage(),
                userAdmissionCreateRequestDto.getDescription()
        )

        this.mockUserDomainModel.setState(UserState.AWAIT)
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.existsByUserId(((UserDomainModel)this.mockUserDomainModel).getId()) >> false

        PowerMockito.mockStatic(UserAdmissionDomainModel.class)
        PowerMockito.when(UserAdmissionDomainModel.of(
                (UserDomainModel)this.mockUserDomainModel,
                userAdmissionCreateRequestDto.getAttachImage(),
                userAdmissionCreateRequestDto.getDescription()
        )).thenReturn(createdUserAdmissionDomainModel)

        this.userAdmissionPort.create(createdUserAdmissionDomainModel) >> createdUserAdmissionDomainModel

        when:
        def userAdmissionResponseDto = this.userService.createAdmission(userAdmissionCreateRequestDto)

        then:
        userAdmissionResponseDto instanceof UserAdmissionResponseDto
        with(userAdmissionResponseDto) {
            getUser().getId() == ((UserDomainModel)this.mockUserDomainModel).getId()
        }
    }

    def "User create admission unauthorized case"() {
        given:
        def userAdmissionCreateRequestDto = new UserAdmissionCreateRequestDto(
                ((UserDomainModel)this.mockUserDomainModel).getEmail(),
                "",
                ""
        )

        def createdUserAdmissionDomainModel = UserAdmissionDomainModel.of(
                (UserDomainModel)this.mockUserDomainModel,
                userAdmissionCreateRequestDto.getAttachImage(),
                userAdmissionCreateRequestDto.getDescription()
        )

        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.existsByUserId(((UserDomainModel)this.mockUserDomainModel).getId()) >> false

        PowerMockito.mockStatic(UserAdmissionDomainModel.class)
        PowerMockito.when(UserAdmissionDomainModel.of(
                (UserDomainModel)this.mockUserDomainModel,
                userAdmissionCreateRequestDto.getAttachImage(),
                userAdmissionCreateRequestDto.getDescription()
        )).thenReturn(createdUserAdmissionDomainModel)

        this.userAdmissionPort.create(createdUserAdmissionDomainModel) >> createdUserAdmissionDomainModel

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

    /**
     * Test cases for user admission accept
     */
    @Test
    def "User accept admission normal case"() {
        given:
        def mockRegisterUserDomainModel = UserDomainModel.of(
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

        def userAdmissionDomainModel = UserAdmissionDomainModel.of(
                "test",
                mockRegisterUserDomainModel,
                "",
                "",
                null,
                null
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.findById("test") >> Optional.of(userAdmissionDomainModel)
        this.userPort.updateRole(mockRegisterUserDomainModel.getId(), Role.COMMON) >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findOldest3Boards() >> List.of()
        this.userAdmissionLogPort.create(
                userAdmissionDomainModel.getUser().getEmail(),
                userAdmissionDomainModel.getUser().getName(),
                mockRegisterUserDomainModel.getEmail(),
                mockRegisterUserDomainModel.getName(),
                UserAdmissionLogAction.ACCEPT,
                userAdmissionDomainModel.getAttachImage(),
                userAdmissionDomainModel.getDescription()
        ) >> null
        this.userAdmissionPort.delete(userAdmissionDomainModel) >> null

        this.userPort.updateState(userAdmissionDomainModel.getUser().getId(), UserState.ACTIVE) >> Optional.of(mockRegisterUserDomainModel)

        when:
        def userAdmissionResponseDto = this.userService.accept("test", "test")

        then:
        userAdmissionResponseDto instanceof UserAdmissionResponseDto
        with(userAdmissionResponseDto) {
            getUser().getId() == mockRegisterUserDomainModel.getId()
        }
    }

    @Test
    def "User accept admission unauthorized case"() {
        given:
        def mockRegisterUserDomainModel = UserDomainModel.of(
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

        def userAdmissionDomainModel = UserAdmissionDomainModel.of(
                "test",
                mockRegisterUserDomainModel,
                "",
                "",
                null,
                null
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.findById("test") >> Optional.of(userAdmissionDomainModel)

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
        def mockRegisterUserDomainModel = UserDomainModel.of(
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

        def userAdmissionDomainModel = UserAdmissionDomainModel.of(
                "test",
                mockRegisterUserDomainModel,
                "",
                "",
                null,
                null
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.findById("test") >> Optional.of(userAdmissionDomainModel)
        this.userAdmissionLogPort.create(
                userAdmissionDomainModel.getUser().getEmail(),
                userAdmissionDomainModel.getUser().getName(),
                mockRegisterUserDomainModel.getEmail(),
                mockRegisterUserDomainModel.getName(),
                UserAdmissionLogAction.REJECT,
                userAdmissionDomainModel.getAttachImage(),
                userAdmissionDomainModel.getDescription()
        ) >> null
        this.userAdmissionPort.delete(userAdmissionDomainModel) >> null

        this.userPort.updateState(userAdmissionDomainModel.getUser().getId(), UserState.REJECT) >> Optional.of(mockRegisterUserDomainModel)

        when:
        def userAdmissionResponseDto = this.userService.reject("test", "test")

        then:
        userAdmissionResponseDto instanceof UserAdmissionResponseDto
        with(userAdmissionResponseDto) {
            getUser().getId() == mockRegisterUserDomainModel.getId()
        }
    }

    @Test
    def "User reject admission unauthorized case"() {
        given:
        def mockRegisterUserDomainModel = UserDomainModel.of(
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

        def userAdmissionDomainModel = UserAdmissionDomainModel.of(
                "test",
                mockRegisterUserDomainModel,
                "",
                "",
                null,
                null
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.userAdmissionPort.findById("test") >> Optional.of(userAdmissionDomainModel)

        when: "request user is not president and admin"
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.userService.reject("test", "test")

        then:
        thrown(UnauthorizedException)
    }
}