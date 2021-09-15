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
import net.causw.domain.model.CircleDomainModel
import net.causw.domain.model.CircleMemberDomainModel
import net.causw.domain.model.CircleMemberStatus
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

@ActiveProfiles(value = "test")
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest([CircleDomainModel.class, CircleMemberDomainModel.class])
class CircleServiceTest extends Specification {
    private UserPort userPort = Mock(UserPort.class)
    private CirclePort circlePort = Mock(CirclePort.class)
    private CircleMemberPort circleMemberPort = Mock(CircleMemberPort.class)
    private JwtTokenProvider jwtTokenProvider = Mock(JwtTokenProvider.class)

    private CircleService circleService = new CircleService(
            this.circlePort,
            this.userPort,
            this.circleMemberPort
    )

    def leader
    def mockCircleDomainModel
    def mockCircleMemberDomainModel

    def setup() {
        this.leader = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.LEADER_CIRCLE,
                null,
                UserState.ACTIVE
        )

        this.mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                (UserDomainModel)this.leader
        )

        this.mockCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel)this.mockCircleDomainModel,
                "test1",
                "test"
        )
    }

    /**
     * Test cases for circle create
     */
    @Test
    def "Circle create normal case"() {
        given:
        def mockCircleCreateRequestDto = new CircleCreateRequestDto(
                "test",
                null,
                "test_description",
                (String)this.leader.getId()
        )

        def mockApiCallUser = UserDomainModel.of(
                "test2",
                "test2@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        PowerMockito.mockStatic(CircleDomainModel.class)
        PowerMockito.when(CircleDomainModel.of(
                null,
                mockCircleCreateRequestDto.getName(),
                mockCircleCreateRequestDto.getMainImage(),
                mockCircleCreateRequestDto.getDescription(),
                false,
                (UserDomainModel)this.leader
        )).thenReturn((CircleDomainModel)this.mockCircleDomainModel)

        this.leader.setRole(Role.COMMON)
        this.userPort.findById("test2") >> Optional.of(mockApiCallUser)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findByName("test") >> Optional.ofNullable(null)

        this.userPort.updateRole("test", Role.LEADER_CIRCLE) >> Optional.of(this.leader)
        this.circlePort.create((CircleDomainModel)this.mockCircleDomainModel, (UserDomainModel)this.leader) >> this.mockCircleDomainModel
        this.circleMemberPort.create((UserDomainModel)this.leader, (CircleDomainModel)this.mockCircleDomainModel) >> this.mockCircleMemberDomainModel
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.MEMBER) >> Optional.of(this.mockCircleMemberDomainModel)

        when:
        def newCircle = this.circleService.create("test2", mockCircleCreateRequestDto)

        then:
        newCircle instanceof CircleResponseDto
        with(newCircle) {
            getName() == "test"
            getMainImage() == null
            getDescription() == "test_description"
            !getIsDeleted()
            getLeaderId() == "test"
        }
    }

    @Test
    def "Circle create duplicate name case"() {
        given:
        def mockCircleCreateRequestDto = new CircleCreateRequestDto(
                "test",
                null,
                "test_description",
                (String)this.leader.getId()
        )

        def mockApiCallUser = UserDomainModel.of(
                "test2",
                "test2@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.leader.setRole(Role.COMMON)
        this.userPort.findById("test2") >> Optional.of(mockApiCallUser)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findByName("test") >> Optional.of(this.mockCircleDomainModel)

        this.userPort.updateRole("test", Role.LEADER_CIRCLE) >> Optional.of(this.leader)
        this.circlePort.create((CircleDomainModel)this.mockCircleDomainModel, (UserDomainModel)this.leader) >> this.mockCircleDomainModel
        this.circleMemberPort.create((UserDomainModel)this.leader, (CircleDomainModel)this.mockCircleDomainModel) >> this.mockCircleMemberDomainModel
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.MEMBER) >> Optional.of(this.mockCircleMemberDomainModel)

        when: "Fail for create: caused by duplicated name"
        this.circleService.create("test2", mockCircleCreateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Circle create unauthorized api call user case"() {
        given:
        def mockCircleCreateRequestDto = new CircleCreateRequestDto(
                "test",
                null,
                "test_description",
                (String)this.leader.getId()
        )

        def mockApiCallUser = UserDomainModel.of(
                "test2",
                "test2@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.COMMON,
                null,
                UserState.ACTIVE
        )

        this.leader.setRole(Role.COMMON)
        this.userPort.findById("test2") >> Optional.of(mockApiCallUser)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findByName("test") >> Optional.ofNullable(null)

        this.leader.setRole(Role.LEADER_CIRCLE)
        this.userPort.updateRole("test", Role.LEADER_CIRCLE) >> Optional.of(this.leader)
        this.circlePort.create((CircleDomainModel)this.mockCircleDomainModel, (UserDomainModel)this.leader) >> this.mockCircleDomainModel
        this.circleMemberPort.create((UserDomainModel)this.leader, (CircleDomainModel)this.mockCircleDomainModel) >> this.mockCircleMemberDomainModel
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.MEMBER) >> Optional.of(this.mockCircleMemberDomainModel)

        when:
        this.circleService.create("test2", mockCircleCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Circle create leader not common case"() {
        given:
        def mockCircleCreateRequestDto = new CircleCreateRequestDto(
                "test",
                null,
                "test_description",
                (String)this.leader.getId()
        )

        def mockApiCallUser = UserDomainModel.of(
                "test2",
                "test2@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test2") >> Optional.of(mockApiCallUser)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findByName("test") >> Optional.ofNullable(null)

        this.userPort.updateRole("test", Role.LEADER_CIRCLE) >> Optional.of(this.leader)
        this.circlePort.create((CircleDomainModel)this.mockCircleDomainModel, (UserDomainModel)this.leader) >> this.mockCircleDomainModel
        this.circleMemberPort.create((UserDomainModel)this.leader, (CircleDomainModel)this.mockCircleDomainModel) >> this.mockCircleMemberDomainModel
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.MEMBER) >> Optional.of(this.mockCircleMemberDomainModel)

        when:
        this.circleService.create("test2", mockCircleCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Circle create leader not active case"() {
        given:
        def mockCircleCreateRequestDto = new CircleCreateRequestDto(
                "test",
                null,
                "test_description",
                (String)this.leader.getId()
        )

        def mockApiCallUser = UserDomainModel.of(
                "test2",
                "test2@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.leader.setState(UserState.WAIT)
        this.leader.setRole(Role.COMMON)
        this.userPort.findById("test2") >> Optional.of(mockApiCallUser)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findByName("test") >> Optional.ofNullable(null)

        this.leader.setRole(Role.LEADER_CIRCLE)
        this.userPort.updateRole("test", Role.LEADER_CIRCLE) >> Optional.of(this.leader)
        this.circlePort.create((CircleDomainModel)this.mockCircleDomainModel, (UserDomainModel)this.leader) >> this.mockCircleDomainModel
        this.circleMemberPort.create((UserDomainModel)this.leader, (CircleDomainModel)this.mockCircleDomainModel) >> this.mockCircleMemberDomainModel
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.MEMBER) >> Optional.of(this.mockCircleMemberDomainModel)

        when:
        this.circleService.create("test2", mockCircleCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for user circle apply
     */
    @Test
    def "User circle apply normal case"() {
        given:
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.ofNullable(null)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.create((UserDomainModel)this.leader, (CircleDomainModel)this.mockCircleDomainModel) >> this.mockCircleMemberDomainModel

        when:
        def applyUser = this.circleService.userApply("test", "test")

        then:
        applyUser instanceof CircleMemberResponseDto
        with(applyUser) {
            getStatus() == CircleMemberStatus.AWAIT
            getUserId() == "test"
            getCircle().getId() == "test"
        }
    }

    @Test
    def "User circle apply circle deleted case"() {
        given:
        this.mockCircleDomainModel.setIsDeleted(true)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.ofNullable(null)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.create((UserDomainModel)this.leader, (CircleDomainModel)this.mockCircleDomainModel) >> this.mockCircleMemberDomainModel

        when:
        this.circleService.userApply("test", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "User circle apply user already member case"() {
        given:
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.create((UserDomainModel)this.leader, (CircleDomainModel)this.mockCircleDomainModel) >> this.mockCircleMemberDomainModel

        when:
        this.circleService.userApply("test", "test")

        then:
        thrown(BadRequestException)
    }


    /**
     * Test cases for accept & reject user
     */
    @Test
    def "Accept & Reject user normal case"() {
        given:
        CircleMemberDomainModel updatedCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel)this.mockCircleDomainModel,
                "test",
                "test"
        )
        this.mockCircleMemberDomainModel.setUserId("test1")
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findById("test") >> Optional.of(this.mockCircleMemberDomainModel)

        this.circleMemberPort.updateStatus("test", CircleMemberStatus.MEMBER) >> Optional.of(updatedCircleMemberDomainModel)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.REJECT) >> Optional.of(updatedCircleMemberDomainModel)

        when: "Accept user"
        updatedCircleMemberDomainModel.setStatus(CircleMemberStatus.MEMBER)
        def acceptUser = this.circleService.acceptUser("test", "test")

        then:
        acceptUser instanceof CircleMemberResponseDto
        acceptUser.getStatus() == CircleMemberStatus.MEMBER

        when: "Reject user"
        updatedCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        def rejectUser = this.circleService.rejectUser("test", "test")

        then:
        rejectUser instanceof CircleMemberResponseDto
        rejectUser.getStatus() == CircleMemberStatus.REJECT
    }

    @Test
    def "Accept & Reject user not authenticated case"() {
        given:
        CircleMemberDomainModel updatedCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel)this.mockCircleDomainModel,
                "test",
                "test"
        )
        this.mockCircleMemberDomainModel.setUserId("test1")
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findById("test") >> Optional.of(this.mockCircleMemberDomainModel)

        this.circleMemberPort.updateStatus("test", CircleMemberStatus.MEMBER) >> Optional.of(updatedCircleMemberDomainModel)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.REJECT) >> Optional.of(updatedCircleMemberDomainModel)

        when: "Accept user with user id who is not a leader"
        updatedCircleMemberDomainModel.setStatus(CircleMemberStatus.MEMBER)
        this.circleService.acceptUser("invalid-test", "test")

        then:
        thrown(UnauthorizedException)

        when: "Reject user with user id who is not a leader"
        updatedCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.circleService.rejectUser("invalid-test", "test")

        then:
        thrown(UnauthorizedException)

    }

    /**
     * Test cases for leave & drop user
     */
    @Test
    def "Leave user normal case"() {
        given:
        CircleMemberDomainModel updatedCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.LEAVE,
                (CircleDomainModel)this.mockCircleDomainModel,
                "test1",
                "test"
        )

        this.circleMemberPort.findByUserIdAndCircleId("test1", "test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.LEAVE) >> Optional.of(updatedCircleMemberDomainModel)

        when:
        def leaveUser = this.circleService.leaveUser("test1", "test")

        then:
        leaveUser instanceof CircleMemberResponseDto
        leaveUser.getStatus() == CircleMemberStatus.LEAVE
    }

    @Test
    def "Leave user invalid case"() {
        given:
        CircleMemberDomainModel updatedCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.LEAVE,
                (CircleDomainModel)this.mockCircleDomainModel,
                "test1",
                "test"
        )

        this.circleMemberPort.findByUserIdAndCircleId("test1", "test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.LEAVE) >> Optional.of(updatedCircleMemberDomainModel)

        when: "Test with invalid user circle status case"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleService.leaveUser("test1", "test")

        then:
        thrown(BadRequestException)

        when: "Test with leader circle id case"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.MEMBER)
        this.circleService.leaveUser("test", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Drop user normal case"() {
        given:
        CircleMemberDomainModel updatedCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.DROP,
                (CircleDomainModel)this.mockCircleDomainModel,
                "test1",
                "test"
        )

        this.circleMemberPort.findByUserIdAndCircleId("test1", "test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.DROP) >> Optional.of(updatedCircleMemberDomainModel)

        when:
        def dropUser = this.circleService.dropUser("test", "test1", "test")

        then:
        dropUser instanceof CircleMemberResponseDto
        dropUser.getStatus() == CircleMemberStatus.DROP
    }

    @Test
    def "Drop user invalid case"() {
        given:
        CircleMemberDomainModel updatedCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.DROP,
                (CircleDomainModel)this.mockCircleDomainModel,
                "test1",
                "test"
        )

        this.circleMemberPort.findByUserIdAndCircleId("test1", "test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.DROP) >> Optional.of(updatedCircleMemberDomainModel)

        when: "Test without leader user id at request user id"
        this.circleService.dropUser("test1", "test1", "test")

        then:
        thrown(UnauthorizedException)

        when: "Test with invalid user circle status case"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleService.dropUser("test", "test1", "test")

        then:
        thrown(BadRequestException)

        when: "Test with leader circle id case for dropped user"
        this.circleService.dropUser("test", "test", "test")

        then:
        thrown(BadRequestException)
    }

}
