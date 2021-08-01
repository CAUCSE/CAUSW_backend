package net.causw.application

import net.causw.adapter.persistence.User
import net.causw.application.dto.UserCreateRequestDto
import net.causw.application.dto.UserDetailDto
import net.causw.application.dto.UserFullDto
import net.causw.application.dto.UserUpdateRequestDto
import net.causw.application.spi.UserPort
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.model.Role
import net.causw.domain.model.UserState
import org.junit.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@ActiveProfiles(value = "test")
class UserServiceTest extends Specification {

    private UserPort userPort
    private UserService userService

    def setup() {
        this.userPort = Mock(UserPort.class)
        this.userService = new UserService(this.userPort)
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

        def mockUserDetailDto = UserDetailDto.from(mockUser)
        def mockUpdatedUserDetailDto = UserDetailDto.from(
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

        this.userPort.findById(id) >> Optional.of(mockUserDetailDto)
        this.userPort.update(id, userUpdateRequestDto) >> mockUpdatedUserDetailDto
        this.userPort.findByEmail("update@cau.ac.kr") >> Optional.ofNullable(null)

        when:
        def userDetailDto = this.userService.update(id, userUpdateRequestDto)

        then:
        userDetailDto instanceof UserDetailDto
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

        def mockUserDetailDto = UserDetailDto.from(mockUser)

        def mockUpdatedUserDetailDto = UserDetailDto.from(
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

        this.userPort.findById(id) >> Optional.of(mockUserDetailDto)
        this.userPort.update(id, userUpdateRequestDto) >> mockUpdatedUserDetailDto
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
        userDetailDto instanceof UserDetailDto
        userDetailDto.getEmail() == "update@cau.ac.kr"
    }

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

        def mockUserDetailDto = UserDetailDto.from(mockUser)

        def mockUpdatedUserDetailDto = UserDetailDto.from(
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

        this.userPort.findById(id) >> Optional.of(mockUserDetailDto)
        this.userPort.update(id, userUpdateRequestDto) >> mockUpdatedUserDetailDto
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
        userDetailDto instanceof UserDetailDto
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
        def mockUserDetailDto = UserDetailDto.from(mockUser)

        def userCreateRequestDto = new UserCreateRequestDto(
                email,
                name,
                password,
                studentId,
                admissionYear
        )

        this.userPort.create(userCreateRequestDto) >> UserDetailDto.from(mockUser)
        this.userPort.findByEmail(email) >> Optional.ofNullable(null)

        when:
        def userDetail = this.userService.signUp(userCreateRequestDto)

        then:
        userDetail instanceof UserDetailDto
        userDetail.getEmail() == mockUserDetailDto.getEmail()
        userDetail.getName() == mockUserDetailDto.getName()
        userDetail.getStudentId() == mockUserDetailDto.getStudentId()
        userDetail.getAdmissionYear() == mockUserDetailDto.getAdmissionYear()
        userDetail.getRole() == mockUserDetailDto.getRole()
        userDetail.getState() == mockUserDetailDto.getState()
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

        this.userPort.create(userCreateRequestDto) >> UserDetailDto.from(mockUser)
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
        userDetail instanceof UserDetailDto
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

        this.userPort.create(userCreateRequestDto) >> UserDetailDto.from(mockUser)
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
        userDetail instanceof UserDetailDto
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

        this.userPort.create(userCreateRequestDto) >> UserDetailDto.from(mockUser)
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

}