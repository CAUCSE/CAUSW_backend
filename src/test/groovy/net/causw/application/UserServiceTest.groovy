package net.causw.application

import net.causw.adapter.persistence.User
import net.causw.application.dto.UserCreateRequestDto
import net.causw.application.dto.UserResponseDto
import net.causw.application.dto.UserFullDto
import net.causw.application.dto.UserUpdateRequestDto
import net.causw.application.dto.UserUpdateRoleRequestDto
import net.causw.application.spi.CirclePort
import net.causw.application.spi.UserPort
import net.causw.config.JwtTokenProvider
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.Role
import net.causw.domain.model.UserState
import org.junit.Test
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

@ActiveProfiles(value = "test")
class UserServiceTest extends Specification {

    private UserPort userPort
    private CirclePort circlePort
    private UserService userService
    private JwtTokenProvider jwtTokenProvider
    private Validator validator

    def setup() {
        this.userPort = Mock(UserPort.class)
        this.circlePort = Mock(CirclePort.class)
        this.jwtTokenProvider = Mock(JwtTokenProvider.class)
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = validatorFactory.getValidator()
        this.userService = new UserService(this.userPort, this.circlePort, this.jwtTokenProvider, this.validator)
    }

    /**
     * Test cases for user role update
     */

    @Test
    def "User role grant normal case"() {
        given:
        def currentId = "test"
        def targetId = "test1"
        def email = "test-normal-mail@cau.ac.kr"
        def name = "test-name"
        def password = "test1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockCurrentUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.PRESIDENT,
                UserState.WAIT
        )
        def mockTargetUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )

        def mockCurrentUserFullDto = UserFullDto.from(mockCurrentUser)
        def mockTargetUserFullDto = UserFullDto.from(mockTargetUser)
        def mockUpdatedUserFullDto = UserFullDto.from(mockTargetUser)

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COUNCIL)
        this.userPort.findById(currentId) >> Optional.of(mockCurrentUserFullDto)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserFullDto)

        mockUpdatedUserFullDto.setRole(Role.COUNCIL)
        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.LEADER_1)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.LEADER_CIRCLE)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.LEADER_ALUMNI)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserFullDto)

        when: "President -> Grant Council"
        mockUpdatedUserFullDto.setRole(Role.COUNCIL)
        def userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userDetailDto instanceof UserResponseDto
        userDetailDto.getRole() == Role.COUNCIL

        when: "President -> Grant Leader_1"
        mockUpdatedUserFullDto.setRole(Role.LEADER_1)
        userUpdateRoleRequestDto.setRole(Role.LEADER_1)
        userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userDetailDto instanceof UserResponseDto
        userDetailDto.getRole() == Role.LEADER_1

        when: "President -> Grant Leader_Circle"
        mockUpdatedUserFullDto.setRole(Role.LEADER_CIRCLE)
        userUpdateRoleRequestDto.setRole(Role.LEADER_CIRCLE)
        userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userDetailDto instanceof UserResponseDto
        userDetailDto.getRole() == Role.LEADER_CIRCLE

        when: "President -> Grant Leader_Alumni"
        mockUpdatedUserFullDto.setRole(Role.LEADER_ALUMNI)
        userUpdateRoleRequestDto.setRole(Role.LEADER_ALUMNI)
        userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userDetailDto instanceof UserResponseDto
        userDetailDto.getRole() == Role.LEADER_ALUMNI
    }

    @Test
    def "User role delegate normal case"() {
        given:
        def currentId = "test"
        def targetId = "test1"
        def email = "test-normal-mail@cau.ac.kr"
        def name = "test-name"
        def password = "test1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockCurrentUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.WAIT
        )
        def mockTargetUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )

        def mockCurrentUserFullDto = UserFullDto.from(mockCurrentUser)
        def mockTargetUserFullDto = UserFullDto.from(mockTargetUser)
        def mockUpdatedUserFullDto = UserFullDto.from(mockTargetUser)

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COUNCIL)
        this.userPort.findById(currentId) >> Optional.of(mockCurrentUserFullDto)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserFullDto)

        mockUpdatedUserFullDto.setRole(Role.PRESIDENT)
        this.userPort.updateRole(targetId, Role.PRESIDENT) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.LEADER_CIRCLE)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.LEADER_ALUMNI)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.COMMON)
        this.userPort.updateRole(currentId, Role.COMMON) >> Optional.of(mockUpdatedUserFullDto)

        when: "President -> Delegate President"
        mockCurrentUserFullDto.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.PRESIDENT)
        mockUpdatedUserFullDto.setRole(Role.PRESIDENT)
        def userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userDetailDto instanceof UserResponseDto
        userDetailDto.getRole() == Role.PRESIDENT

        when: "Leader Alumni -> Delegate Alumni"
        mockCurrentUserFullDto.setRole(Role.LEADER_ALUMNI)
        userUpdateRoleRequestDto.setRole(Role.LEADER_ALUMNI)
        mockUpdatedUserFullDto.setRole(Role.LEADER_ALUMNI)
        userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userDetailDto instanceof UserResponseDto
        userDetailDto.getRole() == Role.LEADER_ALUMNI
    }

    @Test
    def "User role update invalid grantee"() {
        given:
        def currentId = "test"
        def targetId = "test1"
        def email = "test-normal-mail@cau.ac.kr"
        def name = "test-name"
        def password = "test1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockCurrentUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )
        def mockTargetUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )

        def mockCurrentUserFullDto = UserFullDto.from(mockCurrentUser)
        def mockTargetUserFullDto = UserFullDto.from(mockTargetUser)
        def mockUpdatedUserFullDto = UserFullDto.from(mockTargetUser)

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COMMON)
        this.userPort.findById(currentId) >> Optional.of(mockCurrentUserFullDto)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserFullDto)

        mockUpdatedUserFullDto.setRole(Role.COUNCIL)
        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.LEADER_1)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.LEADER_CIRCLE)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.LEADER_ALUMNI)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserFullDto)

        when: "Admin -> Grant something to Admin user"
        mockUpdatedUserFullDto.setRole(Role.ADMIN)
        mockTargetUserFullDto.setRole(Role.ADMIN)
        def userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant something to President user"
        mockUpdatedUserFullDto.setRole(Role.PRESIDENT)
        mockTargetUserFullDto.setRole(Role.PRESIDENT)
        userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant something to Admin user"
        mockUpdatedUserFullDto.setRole(Role.PRESIDENT)
        mockTargetUserFullDto.setRole(Role.ADMIN)
        userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "User role update invalid grantor"() {
        given:
        def currentId = "test"
        def targetId = "test1"
        def email = "test-normal-mail@cau.ac.kr"
        def name = "test-name"
        def password = "test1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockCurrentUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )
        def mockTargetUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )

        def mockCurrentUserFullDto = UserFullDto.from(mockCurrentUser)
        def mockTargetUserFullDto = UserFullDto.from(mockTargetUser)
        def mockUpdatedUserFullDto = UserFullDto.from(mockTargetUser)

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COMMON)
        this.userPort.findById(currentId) >> Optional.of(mockCurrentUserFullDto)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserFullDto)

        mockUpdatedUserFullDto.setRole(Role.COUNCIL)
        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.LEADER_1)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.LEADER_CIRCLE)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserFullDto)
        mockUpdatedUserFullDto.setRole(Role.LEADER_ALUMNI)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserFullDto)

        when: "Admin -> Grant Admin"
        mockUpdatedUserFullDto.setRole(Role.ADMIN)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        def userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant Admin"
        mockUpdatedUserFullDto.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant Admin"
        mockUpdatedUserFullDto.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Leader Circle -> Grant Common"
        mockUpdatedUserFullDto.setRole(Role.LEADER_CIRCLE)
        userUpdateRoleRequestDto.setRole(Role.COMMON)
        userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Leader Alumni -> Grant Common"
        mockUpdatedUserFullDto.setRole(Role.LEADER_ALUMNI)
        userUpdateRoleRequestDto.setRole(Role.COMMON)
        userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

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
        def email = "test-normal-mail@cau.ac.kr"
        def name = "test-name"
        def password = "test1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )

        def mockUserFullDto = UserFullDto.from(mockUser)
        def mockUpdatedUserFullDto = UserFullDto.from(
                User.of("update@cau.ac.kr",
                        name,
                        password,
                        studentId,
                        admissionYear,
                        Role.NONE,
                        UserState.WAIT)
        )
        def userUpdateRequestDto = new UserUpdateRequestDto(
                "update@cau.ac.kr",
                name,
                password,
                studentId,
                admissionYear
        )

        this.userPort.findById(id) >> Optional.of(mockUserFullDto)
        this.userPort.update(id, userUpdateRequestDto) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.findByEmail("update@cau.ac.kr") >> Optional.ofNullable(null)

        when:
        def userDetailDto = this.userService.update(id, userUpdateRequestDto)

        then:
        userDetailDto instanceof UserResponseDto
        userDetailDto.getEmail() == "update@cau.ac.kr"
    }

    @Test
    def "User update invalid password case"() {
        given:
        def id = "test"
        def email = "test-password-mail@cau.ac.kr"
        def name = "test-name"
        def password = ""
        def studentId = "20210000"
        def admissionYear = 2021

        def mockUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )

        def mockUserFullDto = UserFullDto.from(mockUser)
        def mockUpdatedUserFullDto = UserFullDto.from(
                User.of("update@cau.ac.kr",
                        name,
                        password,
                        studentId,
                        admissionYear,
                        Role.NONE,
                        UserState.WAIT)
        )

        def userUpdateRequestDto = new UserUpdateRequestDto(
                "update@cau.ac.kr",
                name,
                password,
                studentId,
                admissionYear
        )

        this.userPort.findById(id) >> Optional.of(mockUserFullDto)
        this.userPort.update(id, userUpdateRequestDto) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.findByEmail("update@cau.ac.kr") >> Optional.ofNullable(null)

        when: "password with short length"
        userUpdateRequestDto.setPassword("test12!")
        this.userService.update(id, userUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without special character"
        userUpdateRequestDto.setPassword("test1234")
        this.userService.update(id, userUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without number"
        userUpdateRequestDto.setPassword("test!!!!")
        this.userService.update(id, userUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without english"
        userUpdateRequestDto.setPassword("1234567!")
        this.userService.update(id, userUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "finally: test for success"
        userUpdateRequestDto.setPassword("test123!")
        def userDetailDto = this.userService.update(id, userUpdateRequestDto)

        then:
        userDetailDto instanceof UserResponseDto
        userDetailDto.getEmail() == "update@cau.ac.kr"
    }

    @Test
    def "User update invalid admission year case"() {
        given:
        def id = "test"
        def email = "test-admission-year@cau.ac.kr"
        def name = "test-name"
        def password = "test123!"
        def studentId = "20210000"
        def admissionYear = 0

        def mockUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )

        def mockUserFullDto = UserFullDto.from(mockUser)
        def mockUpdatedUserFullDto = UserFullDto.from(
                User.of("update@cau.ac.kr",
                        name,
                        password,
                        studentId,
                        admissionYear,
                        Role.NONE,
                        UserState.WAIT)
        )
        def userUpdateRequestDto = new UserUpdateRequestDto(
                "update@cau.ac.kr",
                name,
                password,
                studentId,
                admissionYear
        )

        this.userPort.findById(id) >> Optional.of(mockUserFullDto)
        this.userPort.update(id, userUpdateRequestDto) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.findByEmail("update@cau.ac.kr") >> Optional.ofNullable(null)

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

        when: "finally: test for success"
        userUpdateRequestDto.setAdmissionYear(2021)
        def userDetailDto = this.userService.update(id, userUpdateRequestDto)

        then:
        userDetailDto instanceof UserResponseDto
    }

    /**
     * Test cases for user sign-up
     */

    @Test
    def "User sign-up normal case"() {
        given:
        def email = "test-normal-mail@cau.ac.kr"
        def name = "test-name"
        def password = "test1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )
        def mockUserFullDto = UserFullDto.from(mockUser)

        def userCreateRequestDto = new UserCreateRequestDto(
                email,
                name,
                password,
                studentId,
                admissionYear
        )

        this.userPort.create(userCreateRequestDto) >> UserFullDto.from(mockUser)
        this.userPort.findByEmail(email) >> Optional.ofNullable(null)

        when:
        def userDetail = this.userService.signUp(userCreateRequestDto)

        then:
        userDetail instanceof UserResponseDto
        userDetail.getEmail() == mockUserFullDto.getEmail()
        userDetail.getName() == mockUserFullDto.getName()
        userDetail.getStudentId() == mockUserFullDto.getStudentId()
        userDetail.getAdmissionYear() == mockUserFullDto.getAdmissionYear()
        userDetail.getRole() == mockUserFullDto.getRole()
        userDetail.getState() == mockUserFullDto.getState()
    }

    @Test
    def "User sign-up invalid password case"() {
        given:
        def email = "test-password-mail@cau.ac.kr"
        def name = "test-name"
        def password = ""
        def studentId = "20210000"
        def admissionYear = 2021

        def userCreateRequestDto = new UserCreateRequestDto(
                email,
                name,
                password,
                studentId,
                admissionYear
        )

        def mockUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )

        this.userPort.create(userCreateRequestDto) >> UserFullDto.from(mockUser)
        this.userPort.findByEmail(email) >> Optional.ofNullable(null)

        when: "password with short length"
        userCreateRequestDto.setPassword("test12!")
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without special character"
        userCreateRequestDto.setPassword("test1234")
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without number"
        userCreateRequestDto.setPassword("test!!!!")
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without english"
        userCreateRequestDto.setPassword("1234567!")
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "finally: test for success"
        userCreateRequestDto.setPassword("test123!")
        def userDetail = this.userService.signUp(userCreateRequestDto)

        then:
        userDetail instanceof UserResponseDto
    }

    @Test
    def "User sign-up invalid admission year case"() {
        given:
        def email = "test-admission-year@cau.ac.kr"
        def name = "test-name"
        def password = "test123!"
        def studentId = "20210000"
        def admissionYear = 0

        def userCreateRequestDto = new UserCreateRequestDto(
                email,
                name,
                password,
                studentId,
                admissionYear
        )

        def mockUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )

        this.userPort.create(userCreateRequestDto) >> UserFullDto.from(mockUser)
        this.userPort.findByEmail(email) >> Optional.ofNullable(null)

        when: "admission year with future day"
        userCreateRequestDto.setAdmissionYear(2100)
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "admission year with past day"
        userCreateRequestDto.setAdmissionYear(1971)
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "finally: test for success"
        userCreateRequestDto.setAdmissionYear(2021)
        def userDetail = this.userService.signUp(userCreateRequestDto)

        then:
        userDetail instanceof UserResponseDto
    }

    @Test
    def "User sign-up duplicate email case"() {
        given:
        def email = "duplicated-mail@cau.ac.kr"

        def name = "test-name"
        def password = "test1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def userCreateRequestDto = new UserCreateRequestDto(
                email,
                name,
                password,
                studentId,
                admissionYear
        )

        def mockUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )

        this.userPort.create(userCreateRequestDto) >> UserFullDto.from(mockUser)
        this.userPort.findByEmail(email) >> Optional.of(new UserFullDto(
                null,
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                null,
                UserState.WAIT,
                null,
                null
        ))

        when: "findByEmail() returns object : expected fail"
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "User sign-up invalid parameter"() {
        given:
        def email = "test-normal-mail@cau.ac.kr"
        def name = "test-name"
        def password = "test1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockUser = User.of(
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                UserState.WAIT
        )

        def userCreateRequestDto = new UserCreateRequestDto(
                email,
                name,
                password,
                studentId,
                admissionYear
        )

        this.userPort.create(userCreateRequestDto) >> UserFullDto.from(mockUser)
        this.userPort.findByEmail(email) >> Optional.ofNullable(null)

        when: "Invalid email"
        userCreateRequestDto.setEmail("invalid-email")
        this.userPort.findByEmail("invalid-email") >> Optional.ofNullable(null)
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "Null email"
        userCreateRequestDto.setEmail(null)
        this.userPort.findByEmail(null) >> Optional.ofNullable(null)
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "Blank name"
        userCreateRequestDto.setEmail(email)
        userCreateRequestDto.setName("")
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "Null admission year"
        userCreateRequestDto.setName("test-name")
        userCreateRequestDto.setAdmissionYear(null)
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }
}