package net.causw.application

import net.causw.adapter.persistence.User
import net.causw.application.dto.UserCreateRequestDto
import net.causw.application.dto.UserDetailDto
import net.causw.application.dto.UserFullDto
import net.causw.application.spi.UserPort
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.model.Role
import net.causw.domain.model.UserState
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@ActiveProfiles(value = "test")
@SpringBootTest
class UserServiceTest extends Specification {

    private UserPort userPort
    private UserService userService

    def setup() {
        userPort = Mock()
        userService = new UserService(userPort)
    }

    /**
     * Test cases for user sign-up
     */
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
        def mockUserDetail = UserDetailDto.from(mockUser)

        def userCreateRequestDto = new UserCreateRequestDto(
                email,
                name,
                password,
                studentId,
                admissionYear
        )

        userPort.create(userCreateRequestDto) >> UserDetailDto.from(mockUser)
        userPort.findByEmail(email) >> Optional.ofNullable(null)

        when:
        def userDetail = userService.signUp(userCreateRequestDto)

        then:
        userDetail instanceof UserDetailDto
        userDetail.getEmail() == mockUserDetail.getEmail()
        userDetail.getName() == mockUserDetail.getName()
        userDetail.getStudentId() == mockUserDetail.getStudentId()
        userDetail.getAdmissionYear() == mockUserDetail.getAdmissionYear()
        userDetail.getRole() == mockUserDetail.getRole()
        userDetail.getState() == mockUserDetail.getState()
    }

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

        userPort.create(userCreateRequestDto) >> UserDetailDto.from(mockUser)
        userPort.findByEmail(email) >> Optional.ofNullable(null)

        when: "password with short length"
        userCreateRequestDto.setPassword("test12!")
        userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without special character"
        userCreateRequestDto.setPassword("test1234")
        userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without number"
        userCreateRequestDto.setPassword("test!!!!")
        userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "password with invalid format: without english"
        userCreateRequestDto.setPassword("1234567!")
        userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "finally: test for success"
        userCreateRequestDto.setPassword("test123!")
        def userDetail = userService.signUp(userCreateRequestDto)

        then:
        userDetail instanceof UserDetailDto
    }

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

        userPort.create(userCreateRequestDto) >> UserDetailDto.from(mockUser)
        userPort.findByEmail(email) >> Optional.ofNullable(null)

        when: "admission year with future day"
        userCreateRequestDto.setAdmissionYear(2100)
        userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "admission year with past day"
        userCreateRequestDto.setAdmissionYear(1971)
        userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)

        when: "finally: test for success"
        userCreateRequestDto.setAdmissionYear(2021)
        def userDetail = userService.signUp(userCreateRequestDto)

        then:
        userDetail instanceof UserDetailDto
    }

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

        userPort.create(userCreateRequestDto) >> UserDetailDto.from(mockUser)
        userPort.findByEmail(email) >> Optional.of(new UserFullDto(
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
        userService.signUp(userCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

}