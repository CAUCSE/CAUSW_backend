package net.causw.application

import net.causw.adapter.persistence.Circle
import net.causw.adapter.persistence.CircleMember
import net.causw.adapter.persistence.User
import net.causw.application.dto.*
import net.causw.application.spi.CircleMemberPort
import net.causw.application.spi.CirclePort
import net.causw.application.spi.UserPort
import net.causw.config.JwtTokenProvider
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.CircleMemberStatus
import net.causw.domain.model.Role
import net.causw.domain.model.UserState
import org.junit.Test
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@ActiveProfiles(value = "test")
class CircleServiceTest extends Specification {
    private UserPort userPort
    private CirclePort circlePort
    private CircleMemberPort circleMemberPort
    private CircleService circleService
    private JwtTokenProvider jwtTokenProvider

    def setup() {
        this.userPort = Mock(UserPort.class)
        this.circlePort = Mock(CirclePort.class)
        this.circleMemberPort = Mock(CircleMemberPort.class)
        this.jwtTokenProvider = Mock(JwtTokenProvider.class)
        this.circleService = new CircleService(this.circlePort, this.userPort, this.circleMemberPort)
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

        def circleMemberId = "Test Circle Create CircleMember Id"

        def mockCircleMember = CircleMember.of(
                circleMemberId,
                CircleMemberStatus.AWAIT,
                mockCircle,
                mockLeaderUser
        )

        def mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)
        def mockCircleFullDto = CircleFullDto.from(mockCircle)

        this.circlePort.create(mockCircleCreateRequestDto, mockLeaderUserFullDto) >> mockCircleFullDto

        this.circleMemberPort.create(mockLeaderUserFullDto, mockCircleFullDto) >> mockCircleMemberDto

        mockCircleMember.setStatus(CircleMemberStatus.MEMBER)
        mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.updateStatus(circleMemberId, CircleMemberStatus.MEMBER) >> Optional.of(mockCircleMemberDto)

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

        def mockCircleMember = CircleMember.of(
                CircleMemberStatus.AWAIT,
                mockCircle,
                mockLeaderUser
        )

        this.circlePort.findByName(name) >> Optional.of(mockCircle)
        this.circlePort.create(mockCircleCreateRequestDto, mockLeaderUserFullDto) >> CircleFullDto.from(mockCircle)

        this.circleMemberPort.create(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> CircleMemberResponseDto.from(mockCircleMember)

        mockCircleMember.setStatus(CircleMemberStatus.MEMBER)
        this.circleMemberPort.accept(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> CircleMemberResponseDto.from(mockCircleMember)

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

        def mockCircleMember = CircleMember.of(
                CircleMemberStatus.AWAIT,
                mockCircle,
                mockLeaderUser
        )

        this.circlePort.create(mockCircleCreateRequestDto, mockLeaderUserFullDto) >> CircleFullDto.from(mockCircle)

        this.circleMemberPort.create(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> CircleMemberResponseDto.from(mockCircleMember)

        mockCircleMember.setStatus(CircleMemberStatus.MEMBER)
        this.circleMemberPort.accept(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> CircleMemberResponseDto.from(mockCircleMember)

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

        def mockCircleMember = CircleMember.of(
                CircleMemberStatus.AWAIT,
                mockCircle,
                mockLeaderUser
        )

        this.circlePort.create(mockCircleCreateRequestDto, mockLeaderUserFullDto) >> CircleFullDto.from(mockCircle)

        this.circleMemberPort.create(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> CircleMemberResponseDto.from(mockCircleMember)

        mockCircleMember.setStatus(CircleMemberStatus.MEMBER)
        this.circleMemberPort.accept(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> CircleMemberResponseDto.from(mockCircleMember)

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

        def mockCircleMember = CircleMember.of(
                CircleMemberStatus.AWAIT,
                mockCircle,
                mockLeaderUser
        )

        this.circlePort.create(mockCircleCreateRequestDto, mockLeaderUserFullDto) >> CircleFullDto.from(mockCircle)

        this.circleMemberPort.create(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> CircleMemberResponseDto.from(mockCircleMember)

        mockCircleMember.setStatus(CircleMemberStatus.MEMBER)
        this.circleMemberPort.accept(UserFullDto.from(mockLeaderUser), CircleFullDto.from(mockCircle)) >> CircleMemberResponseDto.from(mockCircleMember)

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

        this.circleMemberPort.findByUserIdAndCircleId(mockUserFullDto.getId(), mockCircleFullDto.getId()) >> Optional.ofNullable(null)

        def mockCircleMember = CircleMember.of(
                CircleMemberStatus.AWAIT,
                mockCircle,
                mockUser
        )
        def mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.create(mockUserFullDto, mockCircleFullDto) >> mockCircleMemberDto

        when:
        def applyUser = this.circleService.userApply(userId, circleId)

        then:
        applyUser instanceof CircleMemberResponseDto
        applyUser.getStatus() == CircleMemberStatus.AWAIT
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

        this.circleMemberPort.findByUserIdAndCircleId(mockUserFullDto.getId(), mockCircleFullDto.getId()) >> Optional.ofNullable(null)

        def mockCircleMember = CircleMember.of(
                CircleMemberStatus.AWAIT,
                mockCircle,
                mockUser
        )
        def mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.create(mockUserFullDto, mockCircleFullDto) >> mockCircleMemberDto

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

        def mockCircleMember = CircleMember.of(
                CircleMemberStatus.MEMBER,
                mockCircle,
                mockUser
        )
        def mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.findByUserIdAndCircleId(mockUserFullDto.getId(), mockCircleFullDto.getId()) >> Optional.of(mockCircleMemberDto)
        this.circleMemberPort.create(mockUserFullDto, mockCircleFullDto) >> mockCircleMemberDto

        when:
        this.circleService.userApply(userId, circleId)

        then:
        thrown(BadRequestException)
    }


    /**
     * Test cases for accept & reject user
     */
    @Test
    def "Accept & Reject user normal case"() {
        given:
        def leaderUserId = "Test Leader Id"
        def leaderUserEmail = "test-leader-email@cau.ac.kr"
        def leaderUserName = "Test Leader Name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def applyUserId = "Test Apply User Id"
        def applyUserEmail = "test-apply-email@cau.ac.kr"
        def applyUserName = "Test Apply User Name"

        def mockLeaderUser = User.of(
                leaderUserId,
                leaderUserEmail,
                leaderUserName,
                password,
                studentId,
                admissionYear,
                Role.LEADER_CIRCLE,
                UserState.ACTIVE
        )

        def mockApplyUser = User.of(
                applyUserId,
                applyUserEmail,
                applyUserName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def circleId = "Test Circle Id"
        def circleName = "Test Circle Name"
        def description = "Testing accept user"
        def mainImage = "something/test/circle/image"

        def mockCircle = Circle.of(
                circleId,
                circleName,
                description,
                mainImage,
                false,
                mockLeaderUser
        )

        def circleMemberId = "Test CircleMember Id"

        def mockCircleMember = CircleMember.of(
                circleMemberId,
                CircleMemberStatus.AWAIT,
                mockCircle,
                mockApplyUser
        )

        def mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.findById(circleMemberId) >> Optional.of(mockCircleMemberDto)

        mockCircleMember.setStatus(CircleMemberStatus.MEMBER)
        mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.updateStatus(circleMemberId, CircleMemberStatus.MEMBER) >> Optional.of(mockCircleMemberDto)


        mockCircleMember.setStatus(CircleMemberStatus.REJECT)
        mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.updateStatus(circleMemberId, CircleMemberStatus.REJECT) >> Optional.of(mockCircleMemberDto)

        when: "Accept user"
        def acceptUser = this.circleService.acceptUser(leaderUserId, circleMemberId)

        then:
        acceptUser instanceof CircleMemberResponseDto
        acceptUser.getStatus() == CircleMemberStatus.MEMBER

        when: "Reject user"
        def rejectUser = this.circleService.rejectUser(leaderUserId, circleMemberId)

        then:
        rejectUser instanceof CircleMemberResponseDto
        rejectUser.getStatus() == CircleMemberStatus.REJECT
    }

    @Test
    def "Accept & Reject user not authenticated case"() {
        given:
        def leaderUserId = "Test Leader Id"
        def leaderUserEmail = "test-leader-email@cau.ac.kr"
        def leaderUserName = "Test Leader Name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def applyUserId = "Test Apply User Id"
        def applyUserEmail = "test-apply-email@cau.ac.kr"
        def applyUserName = "Test Apply User Name"

        def notAllowedUserId = "Test Not a Leader Id"

        def mockLeaderUser = User.of(
                leaderUserId,
                leaderUserEmail,
                leaderUserName,
                password,
                studentId,
                admissionYear,
                Role.LEADER_CIRCLE,
                UserState.ACTIVE
        )

        def mockApplyUser = User.of(
                applyUserId,
                applyUserEmail,
                applyUserName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def circleId = "Test Circle Id"
        def circleName = "Test Circle Name"
        def description = "Testing accept user"
        def mainImage = "something/test/circle/image"

        def mockCircle = Circle.of(
                circleId,
                circleName,
                description,
                mainImage,
                false,
                mockLeaderUser
        )

        def circleMemberId = "Test CircleMember Id"

        def mockCircleMember = CircleMember.of(
                circleMemberId,
                CircleMemberStatus.AWAIT,
                mockCircle,
                mockApplyUser
        )

        def mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.findById(circleMemberId) >> Optional.of(mockCircleMemberDto)

        mockCircleMember.setStatus(CircleMemberStatus.MEMBER)
        mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.updateStatus(circleMemberId, CircleMemberStatus.MEMBER) >> Optional.of(mockCircleMemberDto)


        mockCircleMember.setStatus(CircleMemberStatus.REJECT)
        mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.updateStatus(circleMemberId, CircleMemberStatus.REJECT) >> Optional.of(mockCircleMemberDto)

        when: "Accept user with user id who is not a leader"
        this.circleService.acceptUser(notAllowedUserId, circleMemberId)

        then:
        thrown(UnauthorizedException)

        when: "Successful accept case"
        def acceptUser = this.circleService.acceptUser(leaderUserId, circleMemberId)

        then:
        acceptUser instanceof CircleMemberResponseDto
        acceptUser.getStatus() == CircleMemberStatus.MEMBER

        when: "Reject user with user id who is not a leader"
        this.circleService.rejectUser(notAllowedUserId, circleMemberId)

        then:
        thrown(UnauthorizedException)

        when: "Successful reject case"
        def rejectUser = this.circleService.rejectUser(leaderUserId, circleMemberId)

        then:
        rejectUser instanceof CircleMemberResponseDto
        rejectUser.getStatus() == CircleMemberStatus.REJECT
    }

    /**
     * Test cases for leave & drop user
     */
    @Test
    def "Leave user normal case"() {
        given:
        def leaderUserId = "Test Leader Id"
        def leaderUserEmail = "test-leader-email@cau.ac.kr"
        def leaderUserName = "Test Leader Name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def leaveUserId = "Test Leave User Id"
        def leaveUserEmail = "test-leave-email@cau.ac.kr"
        def leaveUserName = "Test Leave User Name"

        def mockLeaderUser = User.of(
                leaderUserId,
                leaderUserEmail,
                leaderUserName,
                password,
                studentId,
                admissionYear,
                Role.LEADER_CIRCLE,
                UserState.ACTIVE
        )

        def mockLeaveUser = User.of(
                leaveUserId,
                leaveUserEmail,
                leaveUserName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def circleId = "Test Circle Id"
        def circleName = "Test Circle Name"
        def description = "Testing accept user"
        def mainImage = "something/test/circle/image"

        def mockCircle = Circle.of(
                circleId,
                circleName,
                description,
                mainImage,
                false,
                mockLeaderUser
        )

        def circleMemberId = "Test CircleMember Id"

        def mockCircleMember = CircleMember.of(
                circleMemberId,
                CircleMemberStatus.MEMBER,
                mockCircle,
                mockLeaveUser
        )

        def mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.findByUserIdAndCircleId(leaveUserId, circleId) >> Optional.of(mockCircleMemberDto)

        mockCircleMember.setStatus(CircleMemberStatus.LEAVE)
        mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.updateStatus(circleMemberId, CircleMemberStatus.LEAVE) >> Optional.of(mockCircleMemberDto)

        when:
        def leaveUser = this.circleService.leaveUser(leaveUserId, circleId)

        then:
        leaveUser instanceof CircleMemberResponseDto
        leaveUser.getStatus() == CircleMemberStatus.LEAVE
    }

    @Test
    def "Leave user invalid case"() {
        given:
        def leaderUserId = "Test Leader Id"
        def leaderUserEmail = "test-leader-email@cau.ac.kr"
        def leaderUserName = "Test Leader Name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def leaveUserId = "Test Leave User Id"
        def leaveUserEmail = "test-leave-email@cau.ac.kr"
        def leaveUserName = "Test Leave User Name"

        def mockLeaderUser = User.of(
                leaderUserId,
                leaderUserEmail,
                leaderUserName,
                password,
                studentId,
                admissionYear,
                Role.LEADER_CIRCLE,
                UserState.ACTIVE
        )

        def mockLeaveUser = User.of(
                leaveUserId,
                leaveUserEmail,
                leaveUserName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def circleId = "Test Circle Id"
        def circleName = "Test Circle Name"
        def description = "Testing accept user"
        def mainImage = "something/test/circle/image"

        def mockCircle = Circle.of(
                circleId,
                circleName,
                description,
                mainImage,
                false,
                mockLeaderUser
        )

        def circleMemberId = "Test CircleMember Id"
        def mockCircleMember = CircleMember.of(
                circleMemberId,
                CircleMemberStatus.AWAIT,
                mockCircle,
                mockLeaveUser
        )

        def mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)
        this.circleMemberPort.findByUserIdAndCircleId(leaveUserId, circleId) >> Optional.of(mockCircleMemberDto)


        def leaderCircleMemberId = "Test Leader User Circle Id"
        def mockLeaderCircleMember = CircleMember.of(
                leaderCircleMemberId,
                CircleMemberStatus.MEMBER,
                mockCircle,
                mockLeaderUser
        )

        def mockLeaderCircleMemberDto = CircleMemberResponseDto.from(mockLeaderCircleMember)
        this.circleMemberPort.findByUserIdAndCircleId(leaderUserId, circleId) >> Optional.of(mockLeaderCircleMemberDto)

        when: "Test with invalid user circle status case"
        this.circleService.leaveUser(leaveUserId, circleId)

        then:
        thrown(BadRequestException)

        when: "Test with leader circle id case"
        this.circleService.leaveUser(leaderUserId, circleId)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Drop user normal case"() {
        given:
        def leaderUserId = "Test Leader Id"
        def leaderUserEmail = "test-leader-email@cau.ac.kr"
        def leaderUserName = "Test Leader Name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def dropUserId = "Test Drop User Id"
        def dropUserEmail = "test-drop-email@cau.ac.kr"
        def dropUserName = "Test Drop User Name"

        def mockLeaderUser = User.of(
                leaderUserId,
                leaderUserEmail,
                leaderUserName,
                password,
                studentId,
                admissionYear,
                Role.LEADER_CIRCLE,
                UserState.ACTIVE
        )

        def mockDropUser = User.of(
                dropUserId,
                dropUserEmail,
                dropUserName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def circleId = "Test Circle Id"
        def circleName = "Test Circle Name"
        def description = "Testing accept user"
        def mainImage = "something/test/circle/image"

        def mockCircle = Circle.of(
                circleId,
                circleName,
                description,
                mainImage,
                false,
                mockLeaderUser
        )

        def circleMemberId = "Test CircleMember Id"

        def mockCircleMember = CircleMember.of(
                circleMemberId,
                CircleMemberStatus.MEMBER,
                mockCircle,
                mockDropUser
        )

        def mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.findByUserIdAndCircleId(dropUserId, circleId) >> Optional.of(mockCircleMemberDto)

        mockCircleMember.setStatus(CircleMemberStatus.DROP)
        mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)

        this.circleMemberPort.updateStatus(circleMemberId, CircleMemberStatus.DROP) >> Optional.of(mockCircleMemberDto)

        when:
        def dropUser = this.circleService.dropUser(leaderUserId, dropUserId, circleId)

        then:
        dropUser instanceof CircleMemberResponseDto
        dropUser.getStatus() == CircleMemberStatus.DROP
    }

    @Test
    def "Drop user invalid case"() {
        given:
        def leaderUserId = "Test Leader Id"
        def leaderUserEmail = "test-leader-email@cau.ac.kr"
        def leaderUserName = "Test Leader Name"
        def password = "qwer1234!"
        def studentId = "20210000"
        def admissionYear = 2021

        def dropUserId = "Test Drop User Id"
        def dropUserEmail = "test-drop-email@cau.ac.kr"
        def dropUserName = "Test Drop User Name"

        def mockLeaderUser = User.of(
                leaderUserId,
                leaderUserEmail,
                leaderUserName,
                password,
                studentId,
                admissionYear,
                Role.LEADER_CIRCLE,
                UserState.ACTIVE
        )

        def mockDropUser = User.of(
                dropUserId,
                dropUserEmail,
                dropUserName,
                password,
                studentId,
                admissionYear,
                Role.COMMON,
                UserState.ACTIVE
        )

        def circleId = "Test Circle Id"
        def circleName = "Test Circle Name"
        def description = "Testing accept user"
        def mainImage = "something/test/circle/image"

        def mockCircle = Circle.of(
                circleId,
                circleName,
                description,
                mainImage,
                false,
                mockLeaderUser
        )

        def circleMemberId = "Test User Circle Id"
        def mockCircleMember = CircleMember.of(
                circleMemberId,
                CircleMemberStatus.AWAIT,
                mockCircle,
                mockDropUser
        )

        def mockCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)
        this.circleMemberPort.findByUserIdAndCircleId(dropUserId, circleId) >> Optional.of(mockCircleMemberDto)

        def leaderCircleMemberId = "Test Leader User Circle Id"
        def mockLeaderCircleMember = CircleMember.of(
                leaderCircleMemberId,
                CircleMemberStatus.MEMBER,
                mockCircle,
                mockLeaderUser
        )

        def mockLeaderCircleMemberDto = CircleMemberResponseDto.from(mockCircleMember)
        this.circleMemberPort.findByUserIdAndCircleId(leaderUserId, circleId) >> Optional.of(mockLeaderCircleMemberDto)

        when: "Test without leader user id at request user id"
        this.circleService.dropUser(dropUserId, dropUserId, circleId)

        then:
        thrown(UnauthorizedException)

        when: "Test with invalid user status"
        this.circleService.dropUser(leaderUserId, dropUserId, circleId)

        then:
        thrown(BadRequestException)

        when: "Test with leader circle id case for dropped user"
        this.circleService.dropUser(leaderUserId, leaderUserId, circleId)

        then:
        thrown(BadRequestException)
    }

}
