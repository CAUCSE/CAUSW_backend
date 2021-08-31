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

@ActiveProfiles(value = "test")
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

    def mockUser
    def mockUserFullDto

    def setup() {
        this.mockUser = User.of(
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                UserState.WAIT
        )

        this.mockUserFullDto = UserFullDto.from((User)this.mockUser)
    }

    /**
     * Test cases for user role update
     */

    @Test
    def "User role grant normal case"() {
        given:
        def currentId = "test"
        def targetId = "test1"

        def mockTargetUser = User.of(
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.NONE,
                UserState.WAIT
        )

        def mockTargetUserFullDto = UserFullDto.from(mockTargetUser)
        def mockUpdatedUserFullDto = UserFullDto.from(mockTargetUser)

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COUNCIL)

        this.userPort.findById(currentId) >> Optional.of(mockUserFullDto)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserFullDto)

        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserFullDto)

        when: "President -> Grant Council"
        mockUpdatedUserFullDto.setRole(Role.COUNCIL)
        userUpdateRoleRequestDto.setRole(Role.COUNCIL)
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

        def mockTargetUser = User.of(
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                UserState.WAIT
        )

        def mockTargetUserFullDto = UserFullDto.from(mockTargetUser)
        def mockUpdatedUserFullDto = UserFullDto.from(mockTargetUser)

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COUNCIL)

        this.userPort.findById(currentId) >> Optional.of(mockUserFullDto)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserFullDto)

        this.userPort.updateRole(targetId, Role.PRESIDENT) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(currentId, Role.COMMON) >> Optional.of(mockUpdatedUserFullDto)

        when: "President -> Delegate President"
        mockUserFullDto.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.PRESIDENT)
        mockUpdatedUserFullDto.setRole(Role.PRESIDENT)
        def userDetailDto = this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        userDetailDto instanceof UserResponseDto
        userDetailDto.getRole() == Role.PRESIDENT

        when: "Leader Alumni -> Delegate Alumni"
        mockUserFullDto.setRole(Role.LEADER_ALUMNI)
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

        def mockTargetUser = User.of(
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.NONE,
                UserState.WAIT
        )

        def mockTargetUserFullDto = UserFullDto.from(mockTargetUser)
        def mockUpdatedUserFullDto = UserFullDto.from(mockTargetUser)

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COMMON)

        this.userPort.findById(currentId) >> Optional.of(mockUserFullDto)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserFullDto)

        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserFullDto)

        when: "Admin -> Grant something to Admin user"
        mockUpdatedUserFullDto.setRole(Role.ADMIN)
        mockTargetUserFullDto.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant something to President user"
        mockUpdatedUserFullDto.setRole(Role.PRESIDENT)
        mockTargetUserFullDto.setRole(Role.PRESIDENT)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant something to Admin user"
        mockUpdatedUserFullDto.setRole(Role.PRESIDENT)
        mockTargetUserFullDto.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "User role update invalid grantor"() {
        given:
        def currentId = "test"
        def targetId = "test1"

        def mockTargetUser = User.of(
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.NONE,
                UserState.WAIT
        )

        def mockTargetUserFullDto = UserFullDto.from(mockTargetUser)
        def mockUpdatedUserFullDto = UserFullDto.from(mockTargetUser)

        def userUpdateRoleRequestDto = new UserUpdateRoleRequestDto(Role.COMMON)

        mockUserFullDto.setRole(Role.NONE)
        this.userPort.findById(currentId) >> Optional.of(mockUserFullDto)
        this.userPort.findById(targetId) >> Optional.of(mockTargetUserFullDto)

        this.userPort.updateRole(targetId, Role.COUNCIL) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(targetId, Role.LEADER_1) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(targetId, Role.LEADER_CIRCLE) >> Optional.of(mockUpdatedUserFullDto)
        this.userPort.updateRole(targetId, Role.LEADER_ALUMNI) >> Optional.of(mockUpdatedUserFullDto)

        when: "Admin -> Grant Admin"
        mockUpdatedUserFullDto.setRole(Role.ADMIN)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant Admin"
        mockUpdatedUserFullDto.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "President -> Grant Admin"
        mockUpdatedUserFullDto.setRole(Role.PRESIDENT)
        userUpdateRoleRequestDto.setRole(Role.ADMIN)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Leader Circle -> Grant Common"
        mockUpdatedUserFullDto.setRole(Role.LEADER_CIRCLE)
        userUpdateRoleRequestDto.setRole(Role.COMMON)
        this.userService.updateUserRole(currentId, targetId, userUpdateRoleRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "Leader Alumni -> Grant Common"
        mockUpdatedUserFullDto.setRole(Role.LEADER_ALUMNI)
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
        def name = "test-name"
        def password = "test1234!"
        def studentId = "20210000"
        def admissionYear = 2021

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
    def "User update invalid admission year case"() {
        given:
        def id = "test"
        def email = "test@cau.ac.kr"
        def name = "test-name"
        def password = "test123!"
        def studentId = "20210000"

        def mockUpdatedUserFullDto = UserFullDto.from(
                User.of(email,
                        name,
                        password,
                        studentId,
                        2021,
                        Role.NONE,
                        UserState.WAIT)
        )
        def userUpdateRequestDto = new UserUpdateRequestDto(
                email,
                name,
                studentId,
                2021
        )

        this.userPort.findById(id) >> Optional.of(mockUserFullDto)
        this.userPort.update(id, userUpdateRequestDto) >> Optional.of(mockUpdatedUserFullDto)

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

        def userCreateRequestDto = new UserCreateRequestDto(
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021
        )

        this.userPort.create(userCreateRequestDto) >> UserFullDto.from((User)mockUser)
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)

        when:
        def userDetail = this.userService.signUp(userCreateRequestDto)

        then:
        userDetail instanceof UserResponseDto
        with (userDetail) {
            getEmail() == mockUserFullDto.getEmail()
            getName() == mockUserFullDto.getName()
            getStudentId() == mockUserFullDto.getStudentId()
            getAdmissionYear() == mockUserFullDto.getAdmissionYear()
            getRole() == mockUserFullDto.getRole()
            getState() == mockUserFullDto.getState()
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

        this.userPort.create(userCreateRequestDto) >> UserFullDto.from((User)mockUser)
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)

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
        def userCreateRequestDto = new UserCreateRequestDto(
                "test@cau.ac.kr",
                "test",
                "test123!",
                "20210000",
                0
        )

        this.userPort.create(userCreateRequestDto) >> UserFullDto.from((User)mockUser)
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)

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
        def userCreateRequestDto = new UserCreateRequestDto(
                "test@cau.ac.kr",
                "test-name",
                "test1234!",
                "20210000",
                2021
        )

        this.userPort.create(userCreateRequestDto) >> mockUserFullDto
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.of(mockUserFullDto)

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

        this.userPort.create(userCreateRequestDto) >> UserFullDto.from((User)mockUser)
        this.userPort.findByEmail("test@cau.ac.kr") >> Optional.ofNullable(null)

        when: "Invalid email"
        userCreateRequestDto.setEmail("invalid-email")
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "Null email"
        userCreateRequestDto.setEmail(null)
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "Blank name"
        userCreateRequestDto.setEmail("test@cau.ac.kr")
        userCreateRequestDto.setName("")
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "Null admission year"
        userCreateRequestDto.setName("test")
        userCreateRequestDto.setAdmissionYear(null)
        this.userService.signUp(userCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }
}