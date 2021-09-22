package net.causw.application

import net.causw.application.dto.UserCreateRequestDto
import net.causw.application.dto.UserPasswordUpdateRequestDto
import net.causw.application.dto.UserResponseDto
import net.causw.application.dto.UserUpdateRequestDto
import net.causw.application.dto.UserUpdateRoleRequestDto
import net.causw.application.spi.CirclePort
import net.causw.application.spi.UserPort
import net.causw.config.JwtTokenProvider
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.Role
import net.causw.domain.model.UserDomainModel
import net.causw.domain.model.UserState
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

@ActiveProfiles(value = "test")
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest([UserDomainModel.class])
class UserServiceTest extends Specification {
    private UserPort userPort = Mock(UserPort.class)
    private CirclePort circlePort = Mock(CirclePort.class)
    private JwtTokenProvider jwtTokenProvider = Mock(JwtTokenProvider.class)
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private UserService userService = new UserService(
            this.userPort,
            this.circlePort,
            this.jwtTokenProvider,
            this.validator
    )

    def mockUserDomainModel

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
                UserState.WAIT
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
                UserState.WAIT
        )

        def mockUpadtedUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.WAIT
        )

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COUNCIL)

        this.userPort.findById(currentId) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserDomainModel)

        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpadtedUserDomainModel)

        when: "President -> Grant Council"
        mockUpadtedUserDomainModel.setRole(Role.COUNCIL)
        userUpdateRoleRequestDto.setRole(Role.COUNCIL)
        def userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.COUNCIL

        when: "President -> Grant Leader_1"
        mockUpadtedUserDomainModel.setRole(Role.LEADER_1)
        userUpdateRoleRequestDto.setRole(Role.LEADER_1)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_1

        when: "President -> Grant Leader_Circle"
        mockUpadtedUserDomainModel.setRole(Role.LEADER_CIRCLE)
        userUpdateRoleRequestDto.setRole(Role.LEADER_CIRCLE)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_CIRCLE

        when: "President -> Grant Leader_Alumni"
        mockUpadtedUserDomainModel.setRole(Role.LEADER_ALUMNI)
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
                UserState.WAIT
        )

        def mockUpadtedUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.WAIT
        )

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COUNCIL)

        this.userPort.findById(currentId) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserDomainModel)

        this.userPort.updateRole(targetId, Role.PRESIDENT) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(currentId, Role.COMMON) >> Optional.of(mockUpadtedUserDomainModel)

        when: "President -> Delegate President"
        this.mockUserDomainModel.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.PRESIDENT)
        mockUpadtedUserDomainModel.setRole(Role.PRESIDENT)
        def userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.PRESIDENT

        when: "Leader Alumni -> Delegate Alumni"
        this.mockUserDomainModel.setRole(Role.LEADER_ALUMNI)
        userUpdateRoleRequestDto.setRole(Role.LEADER_ALUMNI)
        mockUpadtedUserDomainModel.setRole(Role.LEADER_ALUMNI)
        userResponseDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        userResponseDto.getRole() == Role.LEADER_ALUMNI
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
                UserState.WAIT
        )

        def mockUpadtedUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.WAIT
        )

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COMMON)

        this.userPort.findById(currentId) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserDomainModel)

        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpadtedUserDomainModel)

        when: "Admin -> Grant something to Admin user"
        mockUpadtedUserDomainModel.setRole(Role.ADMIN)
        mockTargetUserDomainModel.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant something to President user"
        mockUpadtedUserDomainModel.setRole(Role.PRESIDENT)
        mockTargetUserDomainModel.setRole(Role.PRESIDENT)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant something to Admin user"
        mockUpadtedUserDomainModel.setRole(Role.PRESIDENT)
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
                UserState.WAIT
        )

        def mockUpadtedUserDomainModel = UserDomainModel.of(
                targetId,
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.WAIT
        )

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COMMON)

        this.mockUserDomainModel.setRole(Role.NONE)
        this.userPort.findById(currentId) >> Optional.of(this.mockUserDomainModel)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserDomainModel)

        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpadtedUserDomainModel)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpadtedUserDomainModel)

        when: "Admin -> Grant Admin"
        mockUpadtedUserDomainModel.setRole(Role.ADMIN)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant Admin"
        mockUpadtedUserDomainModel.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant Admin"
        mockUpadtedUserDomainModel.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Leader Circle -> Grant Common"
        mockUpadtedUserDomainModel.setRole(Role.LEADER_CIRCLE)
        userUpdateRoleRequestDto.setRole(Role.COMMON)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Leader Alumni -> Grant Common"
        mockUpadtedUserDomainModel.setRole(Role.LEADER_ALUMNI)
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
                admissionYear
        )

        def mockUpdatedUserDomainModel = UserDomainModel.of(
                id,
                userUpdateRequestDto.getEmail(),
                userUpdateRequestDto.getName(),
                (String)this.mockUserDomainModel.getPassword(),
                userUpdateRequestDto.getStudentId(),
                userUpdateRequestDto.getAdmissionYear(),
                (Role)this.mockUserDomainModel.getRole(),
                (String)this.mockUserDomainModel.getProfileImage(),
                (UserState)this.mockUserDomainModel.getState()
        )

        PowerMockito.mockStatic(UserDomainModel.class)
        PowerMockito.when(UserDomainModel.of(
                id,
                userUpdateRequestDto.getEmail(),
                userUpdateRequestDto.getName(),
                (String)this.mockUserDomainModel.getPassword(),
                userUpdateRequestDto.getStudentId(),
                userUpdateRequestDto.getAdmissionYear(),
                (Role)this.mockUserDomainModel.getRole(),
                (String)this.mockUserDomainModel.getProfileImage(),
                (UserState)this.mockUserDomainModel.getState()
        )).thenReturn(mockUpdatedUserDomainModel)

        this.userPort.findById(id) >> Optional.of(this.mockUserDomainModel)
        this.userPort.update(id, mockUpdatedUserDomainModel) >> Optional.of(mockUpdatedUserDomainModel)
        this.userPort.findByEmail("update@cau.ac.kr") >> Optional.ofNullable(null)

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
                2021
        )

        def mockUpdatedUserDomainModel = UserDomainModel.of(
                id,
                userUpdateRequestDto.getEmail(),
                userUpdateRequestDto.getName(),
                (String)this.mockUserDomainModel.getPassword(),
                userUpdateRequestDto.getStudentId(),
                userUpdateRequestDto.getAdmissionYear(),
                (Role)this.mockUserDomainModel.getRole(),
                (String)this.mockUserDomainModel.getProfileImage(),
                (UserState)this.mockUserDomainModel.getState()
        )

        this.userPort.findById(id) >> Optional.of(this.mockUserDomainModel)
        this.userPort.update(id, mockUpdatedUserDomainModel) >> Optional.of(mockUpdatedUserDomainModel)

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
                2021
        )

        PowerMockito.mockStatic(UserDomainModel.class)
        PowerMockito.when(UserDomainModel.of(
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                null,
        )).thenReturn((UserDomainModel)this.mockUserDomainModel)

        this.userPort.create((UserDomainModel)this.mockUserDomainModel) >> this.mockUserDomainModel
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)

        when:
        def userResponseDto = this.userService.signUp(userCreateRequestDto)

        then:
        userResponseDto instanceof UserResponseDto
        with (userResponseDto) {
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
                2021
        )

        def mockCreatedUserDomainModel = UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                null,
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
                0
        )

        def mockCreatedUserDomainModel = UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                null
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
                2021
        )

        def mockCreatedUserDomainModel = UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                null,
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
                2021
        )

        def mockCreatedUserDomainModel = UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                null
        )

        this.userPort.create(mockCreatedUserDomainModel) >> mockCreatedUserDomainModel
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)

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
                null,
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
                null
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
                null
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
                null
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
                (String)this.mockUserDomainModel.getId(),
                (String)this.mockUserDomainModel.getEmail(),
                (String)this.mockUserDomainModel.getName(),
                "test12345!",
                (String)this.mockUserDomainModel.getStudentId(),
                (Integer)this.mockUserDomainModel.getAdmissionYear(),
                Role.PRESIDENT,
                null,
                UserState.WAIT
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
}