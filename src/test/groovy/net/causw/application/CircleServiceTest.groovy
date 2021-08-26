package net.causw.application

import net.causw.adapter.persistence.Circle
import net.causw.adapter.persistence.User
import net.causw.adapter.persistence.UserCircle
import net.causw.application.dto.*
import net.causw.application.spi.CirclePort
import net.causw.application.spi.UserCirclePort
import net.causw.application.spi.UserPort
import net.causw.config.JwtTokenProvider
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.Role
import net.causw.domain.model.UserCircleStatus
import net.causw.domain.model.UserState
import org.junit.Test
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@ActiveProfiles(value = "test")
class CircleServiceTest extends Specification {
    private UserPort userPort
    private CirclePort circlePort
    private UserCirclePort userCirclePort
    private CircleService circleService
    private JwtTokenProvider jwtTokenProvider

    def setup() {
        this.userPort = Mock(UserPort.class)
        this.circlePort = Mock(CirclePort.class)
        this.userCirclePort = Mock(UserCirclePort.class)
        this.jwtTokenProvider = Mock(JwtTokenProvider.class)
        this.circleService = new CircleService(this.circlePort, this.userPort, this.userCirclePort)
    }

    /**
     * Test cases for circle create
     */
    @Test
    def "Circle create normal case"() {
        given:
        def id = "Test Circle Id"
        def name = "Test Circle Name"
        def description = "Testing circle create, this is description"
        def mainImage = "something/test/circle/image"

        def leaderId = "test"
        def apiCallUserId = "test2"
        def email = "test-email@cau.ac.kr"
        def userName = "test-name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockCircleCreateRequestDto = new CircleCreateRequestDto(
                name,
                mainImage,
                description,
                leaderId
        )

        def mockLeaderUser = User.of(
                leaderId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def mockApiCallUser = User.of(
                apiCallUserId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.PRESIDENT,
                UserState.ACTIVE
        )

        def mockApiCallUserFullDto = UserFullDto.from(mockApiCallUser)
        def mockLeaderUserFullDto = UserFullDto.from(mockLeaderUser)

        this.userPort.findById(apiCallUserId) >> Optional.of(mockApiCallUserFullDto)
        this.userPort.findById(leaderId) >> Optional.of(mockLeaderUserFullDto)
        this.circlePort.findByName(name) >> Optional.ofNullable(null)

        mockLeaderUser.setRole(Role.LEADER_CIRCLE)
        mockLeaderUserFullDto = UserFullDto.from(mockLeaderUser)
        this.userPort.updateRole(leaderId, Role.LEADER_CIRCLE) >> Optional.of(mockLeaderUserFullDto)

        def mockCircle = Circle.of(
                id,
                name,
                mainImage,
                description,
                false,
                mockLeaderUser
        )

        def mockUserCircle = UserCircle.of(
                UserCircleStatus.AWAIT,
                mockCircle,
                mockLeaderUser
        )

        this.circlePort.create(mockCircleCreateRequestDto, mockLeaderUserFullDto) >> CircleFullDto.from(mockCircle)

        this.userCirclePort.create(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> UserCircleDto.from(mockUserCircle)

        mockUserCircle.setStatus(UserCircleStatus.MEMBER)
        this.userCirclePort.accept(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> UserCircleDto.from(mockUserCircle)

        when:
        def newCircle = this.circleService.create(apiCallUserId, mockCircleCreateRequestDto)

        then:
        newCircle instanceof CircleResponseDto
        newCircle.getName() == name
        newCircle.getMainImage() == mainImage
        newCircle.getDescription() == description
        !newCircle.getIsDeleted()
        newCircle.getManager().getId() == leaderId
    }

    @Test
    def "Circle create duplicate name case"() {
        given:
        def id = "Test Circle Id"
        def name = "Test Circle Duplicate Name"
        def dupName = "Test Circle Duplicate Name"
        def description = "Testing circle create, this is description"
        def mainImage = "something/test/circle/image"

        def leaderId = "test"
        def apiCallUserId = "test2"
        def email = "test-email@cau.ac.kr"
        def userName = "test-name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockCircleCreateRequestDto = new CircleCreateRequestDto(
                name,
                mainImage,
                description,
                leaderId
        )

        def mockLeaderUser = User.of(
                leaderId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def mockApiCallUser = User.of(
                apiCallUserId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.PRESIDENT,
                UserState.ACTIVE
        )

        def mockApiCallUserFullDto = UserFullDto.from(mockApiCallUser)
        def mockLeaderUserFullDto = UserFullDto.from(mockLeaderUser)

        this.userPort.findById(apiCallUserId) >> Optional.of(mockApiCallUserFullDto)
        this.userPort.findById(leaderId) >> Optional.of(mockLeaderUserFullDto)

        mockLeaderUser.setRole(Role.LEADER_CIRCLE)
        mockLeaderUserFullDto = UserFullDto.from(mockLeaderUser)
        this.userPort.updateRole(leaderId, Role.LEADER_CIRCLE) >> Optional.of(mockLeaderUserFullDto)

        def mockCircle = Circle.of(
                id,
                name,
                mainImage,
                description,
                false,
                mockLeaderUser
        )

        def mockUserCircle = UserCircle.of(
                UserCircleStatus.AWAIT,
                mockCircle,
                mockLeaderUser
        )

        this.circlePort.findByName(name) >> Optional.of(mockCircle)
        this.circlePort.create(mockCircleCreateRequestDto, mockLeaderUserFullDto) >> CircleFullDto.from(mockCircle)

        this.userCirclePort.create(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> UserCircleDto.from(mockUserCircle)

        mockUserCircle.setStatus(UserCircleStatus.MEMBER)
        this.userCirclePort.accept(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> UserCircleDto.from(mockUserCircle)

        when: "Fail for create: caused by duplicated name"
        def newCircle = this.circleService.create(apiCallUserId, mockCircleCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Circle create unauthorized api call user case"() {
        given:
        def id = "Test Circle Id"
        def name = "Test Circle Name"
        def description = "Testing circle create, this is description"
        def mainImage = "something/test/circle/image"

        def leaderId = "test"
        def apiCallUserId = "test2"
        def email = "test-email@cau.ac.kr"
        def userName = "test-name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockCircleCreateRequestDto = new CircleCreateRequestDto(
                name,
                mainImage,
                description,
                leaderId
        )

        def mockLeaderUser = User.of(
                leaderId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def mockApiCallUser = User.of(
                apiCallUserId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.LEADER_CIRCLE,
                UserState.ACTIVE
        )

        def mockApiCallUserFullDto = UserFullDto.from(mockApiCallUser)
        def mockLeaderUserFullDto = UserFullDto.from(mockLeaderUser)

        this.userPort.findById(apiCallUserId) >> Optional.of(mockApiCallUserFullDto)
        this.userPort.findById(leaderId) >> Optional.of(mockLeaderUserFullDto)
        this.circlePort.findByName(name) >> Optional.ofNullable(null)

        mockLeaderUser.setRole(Role.LEADER_CIRCLE)
        mockLeaderUserFullDto = UserFullDto.from(mockLeaderUser)
        this.userPort.updateRole(leaderId, Role.LEADER_CIRCLE) >> Optional.of(mockLeaderUserFullDto)

        def mockCircle = Circle.of(
                id,
                name,
                mainImage,
                description,
                false,
                mockLeaderUser
        )

        def mockUserCircle = UserCircle.of(
                UserCircleStatus.AWAIT,
                mockCircle,
                mockLeaderUser
        )

        this.circlePort.create(mockCircleCreateRequestDto, mockLeaderUserFullDto) >> CircleFullDto.from(mockCircle)

        this.userCirclePort.create(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> UserCircleDto.from(mockUserCircle)

        mockUserCircle.setStatus(UserCircleStatus.MEMBER)
        this.userCirclePort.accept(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> UserCircleDto.from(mockUserCircle)

        when:
        this.circleService.create(apiCallUserId, mockCircleCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Circle create leader not common case"() {
        given:
        def id = "Test Circle Id"
        def name = "Test Circle Name"
        def description = "Testing circle create, this is description"
        def mainImage = "something/test/circle/image"

        def leaderId = "test"
        def apiCallUserId = "test2"
        def email = "test-email@cau.ac.kr"
        def userName = "test-name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockCircleCreateRequestDto = new CircleCreateRequestDto(
                name,
                mainImage,
                description,
                leaderId
        )

        def mockLeaderUser = User.of(
                leaderId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.LEADER_1,
                UserState.ACTIVE
        )

        def mockApiCallUser = User.of(
                apiCallUserId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.PRESIDENT,
                UserState.ACTIVE
        )

        def mockApiCallUserFullDto = UserFullDto.from(mockApiCallUser)
        def mockLeaderUserFullDto = UserFullDto.from(mockLeaderUser)

        this.userPort.findById(apiCallUserId) >> Optional.of(mockApiCallUserFullDto)
        this.userPort.findById(leaderId) >> Optional.of(mockLeaderUserFullDto)
        this.circlePort.findByName(name) >> Optional.ofNullable(null)

        mockLeaderUser.setRole(Role.LEADER_CIRCLE)
        mockLeaderUserFullDto = UserFullDto.from(mockLeaderUser)
        this.userPort.updateRole(leaderId, Role.LEADER_CIRCLE) >> Optional.of(mockLeaderUserFullDto)

        def mockCircle = Circle.of(
                id,
                name,
                mainImage,
                description,
                false,
                mockLeaderUser
        )

        def mockUserCircle = UserCircle.of(
                UserCircleStatus.AWAIT,
                mockCircle,
                mockLeaderUser
        )

        this.circlePort.create(mockCircleCreateRequestDto, mockLeaderUserFullDto) >> CircleFullDto.from(mockCircle)

        this.userCirclePort.create(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> UserCircleDto.from(mockUserCircle)

        mockUserCircle.setStatus(UserCircleStatus.MEMBER)
        this.userCirclePort.accept(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> UserCircleDto.from(mockUserCircle)

        when:
        this.circleService.create(apiCallUserId, mockCircleCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Circle create leader not active case"() {
        given:
        def id = "Test Circle Id"
        def name = "Test Circle Name"
        def description = "Testing circle create, this is description"
        def mainImage = "something/test/circle/image"

        def leaderId = "test"
        def apiCallUserId = "test2"
        def email = "test-email@cau.ac.kr"
        def userName = "test-name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockCircleCreateRequestDto = new CircleCreateRequestDto(
                name,
                mainImage,
                description,
                leaderId
        )

        def mockLeaderUser = User.of(
                leaderId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.INACTIVE
        )

        def mockApiCallUser = User.of(
                apiCallUserId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.PRESIDENT,
                UserState.ACTIVE
        )

        def mockApiCallUserFullDto = UserFullDto.from(mockApiCallUser)
        def mockLeaderUserFullDto = UserFullDto.from(mockLeaderUser)

        this.userPort.findById(apiCallUserId) >> Optional.of(mockApiCallUserFullDto)
        this.userPort.findById(leaderId) >> Optional.of(mockLeaderUserFullDto)
        this.circlePort.findByName(name) >> Optional.ofNullable(null)

        mockLeaderUser.setRole(Role.LEADER_CIRCLE)
        mockLeaderUserFullDto = UserFullDto.from(mockLeaderUser)
        this.userPort.updateRole(leaderId, Role.LEADER_CIRCLE) >> Optional.of(mockLeaderUserFullDto)

        def mockCircle = Circle.of(
                id,
                name,
                mainImage,
                description,
                false,
                mockLeaderUser
        )

        def mockUserCircle = UserCircle.of(
                UserCircleStatus.AWAIT,
                mockCircle,
                mockLeaderUser
        )

        this.circlePort.create(mockCircleCreateRequestDto, mockLeaderUserFullDto) >> CircleFullDto.from(mockCircle)

        this.userCirclePort.create(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> UserCircleDto.from(mockUserCircle)

        mockUserCircle.setStatus(UserCircleStatus.MEMBER)
        this.userCirclePort.accept(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> UserCircleDto.from(mockUserCircle)

        when:
        this.circleService.create(apiCallUserId, mockCircleCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for user circle apply
     */
    @Test
    def "User circle apply normal case"() {
        given:
        def circleId = "Test Circle Id"
        def circleName = "Test Circle Name"
        def circleDescription = "Testing User Circle Apply"
        def circleMainImage = "something/test/circle/image"

        def userId = "Test User Id"
        def leaderId = "Test Leader Id"
        def email = "test-email@cau.ac.kr"
        def userName = "test-name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockLeaderUser = User.of(
                leaderId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.LEADER_CIRCLE,
                UserState.ACTIVE
        )

        def mockUser = User.of(
                userId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def mockCircle = Circle.of(
                circleId,
                circleName,
                circleMainImage,
                circleDescription,
                false,
                mockLeaderUser
        )

        def mockCircleFullDto = CircleFullDto.from(mockCircle)
        def mockUserFullDto = UserFullDto.from(mockUser)

        this.circlePort.findById(circleId) >> Optional.of(mockCircleFullDto)
        this.userPort.findById(userId) >> Optional.of(mockUserFullDto)

        this.userCirclePort.loadUserCircleStatus(mockUserFullDto, mockCircleFullDto) >> Optional.ofNullable(null)

        def mockUserCircle = UserCircle.of(
                UserCircleStatus.AWAIT,
                mockCircle,
                mockUser
        )
        def mockUserCircleDto = UserCircleDto.from(mockUserCircle)

        this.userCirclePort.create(mockUserFullDto, mockCircleFullDto) >> mockUserCircleDto

        when:
        def applyUser = this.circleService.userApply(userId, circleId)

        then:
        applyUser instanceof UserCircleDto
        applyUser.getStatus() == UserCircleStatus.AWAIT
        applyUser.getUser().getId() == userId
        applyUser.getCircle().getId() == circleId
    }

    @Test
    def "User circle apply circle deleted case"() {
        given:
        def circleId = "Test Circle Id"
        def circleName = "Test Circle Name"
        def circleDescription = "Testing User Circle Apply"
        def circleMainImage = "something/test/circle/image"

        def userId = "Test User Id"
        def leaderId = "Test Leader Id"
        def email = "test-email@cau.ac.kr"
        def userName = "test-name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockLeaderUser = User.of(
                leaderId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.LEADER_CIRCLE,
                UserState.ACTIVE
        )

        def mockUser = User.of(
                userId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def mockCircle = Circle.of(
                circleId,
                circleName,
                circleMainImage,
                circleDescription,
                true,
                mockLeaderUser
        )

        def mockCircleFullDto = CircleFullDto.from(mockCircle)
        def mockUserFullDto = UserFullDto.from(mockUser)

        this.circlePort.findById(circleId) >> Optional.of(mockCircleFullDto)
        this.userPort.findById(userId) >> Optional.of(mockUserFullDto)

        this.userCirclePort.loadUserCircleStatus(mockUserFullDto, mockCircleFullDto) >> Optional.ofNullable(null)

        def mockUserCircle = UserCircle.of(
                UserCircleStatus.AWAIT,
                mockCircle,
                mockUser
        )
        def mockUserCircleDto = UserCircleDto.from(mockUserCircle)

        this.userCirclePort.create(mockUserFullDto, mockCircleFullDto) >> mockUserCircleDto

        when:
        this.circleService.userApply(userId, circleId)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "User circle apply user already member case"() {
        given:
        def circleId = "Test Circle Id"
        def circleName = "Test Circle Name"
        def circleDescription = "Testing User Circle Apply"
        def circleMainImage = "something/test/circle/image"

        def userId = "Test User Id"
        def leaderId = "Test Leader Id"
        def email = "test-email@cau.ac.kr"
        def userName = "test-name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def mockLeaderUser = User.of(
                leaderId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.LEADER_CIRCLE,
                UserState.ACTIVE
        )

        def mockUser = User.of(
                userId,
                email,
                userName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def mockCircle = Circle.of(
                circleId,
                circleName,
                circleMainImage,
                circleDescription,
                false,
                mockLeaderUser
        )

        def mockCircleFullDto = CircleFullDto.from(mockCircle)
        def mockUserFullDto = UserFullDto.from(mockUser)

        this.circlePort.findById(circleId) >> Optional.of(mockCircleFullDto)
        this.userPort.findById(userId) >> Optional.of(mockUserFullDto)

        def mockUserCircle = UserCircle.of(
                UserCircleStatus.MEMBER,
                mockCircle,
                mockUser
        )
        def mockUserCircleDto = UserCircleDto.from(mockUserCircle)

        this.userCirclePort.loadUserCircleStatus(mockUserFullDto, mockCircleFullDto) >> Optional.of(mockUserCircle.getStatus())
        this.userCirclePort.create(mockUserFullDto, mockCircleFullDto) >> mockUserCircleDto

        when:
        this.circleService.userApply(userId, circleId)

        then:
        thrown(BadRequestException)
    }
}
