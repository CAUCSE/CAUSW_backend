package net.causw.application

import net.causw.application.dto.circle.CircleCreateRequestDto
import net.causw.application.dto.circle.CircleMemberResponseDto
import net.causw.application.dto.circle.CircleResponseDto
import net.causw.application.dto.circle.CircleUpdateRequestDto
import net.causw.application.dto.circle.CircleBoardsResponseDto
import net.causw.application.dto.circle.CirclesResponseDto
import net.causw.application.spi.BoardPort
import net.causw.application.spi.CircleMemberPort
import net.causw.application.spi.CirclePort
import net.causw.application.spi.CommentPort
import net.causw.application.spi.PostPort
import net.causw.application.spi.UserPort
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.*
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
@PrepareForTest([CircleDomainModel.class, CircleMemberDomainModel.class])
class CircleServiceTest extends Specification {
    private UserPort userPort = Mock(UserPort.class)
    private CirclePort circlePort = Mock(CirclePort.class)
    private CircleMemberPort circleMemberPort = Mock(CircleMemberPort.class)
    private BoardPort boardPort = Mock(BoardPort.class)
    private PostPort postPort = Mock(PostPort.class)
    private CommentPort commentPort = Mock(CommentPort.class)
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()

    private CircleService circleService = new CircleService(
            this.circlePort,
            this.userPort,
            this.circleMemberPort,
            this.boardPort,
            this.postPort,
            this.commentPort,
            this.validator
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
                "/test",
                "test_description",
                false,
                (UserDomainModel) this.leader
        )

        this.mockCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                "test1",
                "test",
                null,
                null
        )
    }

    /**
     * Test cases for circle create
     */
    @Test
    def "Circle find by id normal case"() {
        given:
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)

        when:
        def circleResponseDto = this.circleService.findById("test")

        then:
        circleResponseDto instanceof CircleResponseDto
        with(circleResponseDto) {
            getId() == "test"
        }
    }

    @Test
    def "Circle find by id already deleted"() {
        given:
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)

        when:
        this.mockCircleDomainModel.setIsDeleted(true)
        this.circleService.findById("test")

        then:
        thrown(BadRequestException)
    }

    /**
     * Test cases for circle create
     */
    @Test
    def "Circle find all normal case"() {
        given:
        def circleForAdmin = CircleDomainModel.of(
                "test1",
                "test",
                "/test",
                "test_description",
                false,
                (UserDomainModel) this.leader
        )

        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circleMemberPort.findCircleByUserId("test") >> Map.of("test", this.mockCircleMemberDomainModel)
        this.circlePort.findAll() >> List.of(circleForAdmin, (CircleDomainModel)this.mockCircleDomainModel)

        when: "without admin"
        def circlesResponseDtoList = this.circleService.findAll("test")

        then:
        circlesResponseDtoList instanceof List<CirclesResponseDto>
        println(circlesResponseDtoList)
        with(circlesResponseDtoList) {
            get(0).getId() == "test1"
            (!get(0).getIsJoined())
        }

        when: "with admin"
        this.leader.setRole(Role.ADMIN)
        circlesResponseDtoList = this.circleService.findAll("test")

        then:
        circlesResponseDtoList instanceof List<CirclesResponseDto>
        with(circlesResponseDtoList) {
            get(0).getId() == "test1"
            get(0).getIsJoined()
        }
    }

    /**
     * Test cases for find boards
     */
    @Test
    def "Circle find boards normal case"() {
        given:
        def mockBoardDomainModel = BoardDomainModel.of(
                "test",
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                false,
                (CircleDomainModel) this.mockCircleDomainModel
        )

        def mockPostDomainModel = PostDomainModel.of(
                "test post id",
                "test post title",
                "test post content",
                (UserDomainModel) this.leader,
                false,
                mockBoardDomainModel,
                null,
                null,
                List.of()
        )

        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.boardPort.findByCircleId("test") >> List.of(mockBoardDomainModel)
        this.postPort.findLatest("test") >> Optional.of(mockPostDomainModel)
        this.commentPort.countByPostId("test post id") >> 0

        when: "without admin"
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)
        def circleBoardsResponseDto = this.circleService.findBoards("test", "test")

        then:
        circleBoardsResponseDto instanceof CircleBoardsResponseDto
        with(circleBoardsResponseDto) {
            getCircle().getId() == "test"
        }

        when: "with admin"
        this.leader.setId("test1")
        this.leader.setRole(Role.ADMIN)
        this.userPort.findById("test1") >> Optional.of(this.leader)
        circleBoardsResponseDto = this.circleService.findBoards("test1", "test")

        then:
        circleBoardsResponseDto instanceof CircleBoardsResponseDto
        with(circleBoardsResponseDto) {
            getCircle().getId() == "test"
        }
    }

    @Test
    def "Circle find boards by circle circle already deleted"() {
        given:
        this.mockCircleDomainModel.setIsDeleted(true)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)

        when:
        this.circleService.findBoards("test", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Circle find boards invalid circle member state"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)

        when: "Circle member is await"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleService.findBoards("test", "test")

        then:
        thrown(BadRequestException)

        when: "Circle member is drop"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.DROP)
        this.circleService.findBoards("test", "test")

        then:
        thrown(UnauthorizedException)

        when: "Circle member is leave"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.LEAVE)
        this.circleService.findBoards("test", "test")

        then:
        thrown(BadRequestException)

        when: "Circle member is reject"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.circleService.findBoards("test", "test")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for get user list
     */
    @Test
    def "Circle get user list normal case"() {
        given:
        def circleMember = UserDomainModel.of(
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

        this.userPort.findById("test") >> Optional.of(this.leader)
        this.userPort.findById("test1") >> Optional.of(circleMember)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circleMemberPort.findByCircleId("test", CircleMemberStatus.MEMBER) >> List.of(this.mockCircleMemberDomainModel)

        when:
        def circleMemberResponseDtoList = this.circleService.getUserList("test", "test", CircleMemberStatus.MEMBER)

        then:
        circleMemberResponseDtoList instanceof List<CircleMemberResponseDto>
        with(circleMemberResponseDtoList) {
            get(0).getUser().getId() == "test1"
            get(0).getStatus() == CircleMemberStatus.MEMBER
        }
    }

    @Test
    def "Circle get user list of circle already deleted"() {
        given:
        this.mockCircleDomainModel.setIsDeleted(true)

        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circleMemberPort.findByCircleId("test", CircleMemberStatus.MEMBER) >> List.of(this.mockCircleMemberDomainModel)

        when:
        this.circleService.getUserList("test", "test", CircleMemberStatus.MEMBER)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Circle get user list unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circleMemberPort.findByCircleId("test", CircleMemberStatus.MEMBER) >> List.of(this.mockCircleMemberDomainModel)

        when:
        this.leader.setRole(Role.COMMON)
        this.circleService.getUserList("test", "test", CircleMemberStatus.MEMBER)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for circle create
     */
    @Test
    def "Circle create normal case"() {
        given:
        def mockCircleCreateRequestDto = new CircleCreateRequestDto(
                "test",
                "/test",
                "test_description",
                (String) this.leader.getId()
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
                mockCircleCreateRequestDto.getName(),
                mockCircleCreateRequestDto.getMainImage(),
                mockCircleCreateRequestDto.getDescription(),
                (UserDomainModel) this.leader
        )).thenReturn((CircleDomainModel) this.mockCircleDomainModel)

        this.leader.setRole(Role.COMMON)
        this.userPort.findById("test2") >> Optional.of(mockApiCallUser)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findByName("test") >> Optional.ofNullable(null)

        this.userPort.updateRole("test", Role.LEADER_CIRCLE) >> Optional.of(this.leader)
        this.circlePort.create((CircleDomainModel) this.mockCircleDomainModel) >> this.mockCircleDomainModel
        this.circleMemberPort.create((UserDomainModel) this.leader, (CircleDomainModel) this.mockCircleDomainModel) >> this.mockCircleMemberDomainModel
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.MEMBER) >> Optional.of(this.mockCircleMemberDomainModel)

        when:
        def newCircle = this.circleService.create("test2", mockCircleCreateRequestDto)

        then:
        newCircle instanceof CircleResponseDto
        with(newCircle) {
            getName() == "test"
            getMainImage() == "/test"
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
                "/test",
                "test_description",
                (String) this.leader.getId()
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
                "/test",
                "test_description",
                (String) this.leader.getId()
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
                "/test",
                "test_description",
                (String) this.leader.getId()
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

        when:
        this.circleService.create("test2", mockCircleCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for circle update
     */
    @Test
    def "Circle update normal case"() {
        given:
        def mockCircleUpdateRequestDto = new CircleUpdateRequestDto(
                "test2",
                "/test",
                "test_update_description"
        )

        this.mockCircleDomainModel.setName("test2")
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circlePort.findByName("test2") >> Optional.ofNullable(null)
        this.circlePort.update("test", (CircleDomainModel)this.mockCircleDomainModel) >> Optional.of(this.mockCircleDomainModel)

        when:
        def circleResponseDto = this.circleService.update("test", "test", mockCircleUpdateRequestDto)

        then:
        circleResponseDto instanceof CircleResponseDto
        with(circleResponseDto) {
            getName() == "test2"
        }
    }

    @Test
    def "Circle update duplicate name"() {
        given:
        def mockCircleUpdateRequestDto = new CircleUpdateRequestDto(
                "test2",
                "/test",
                "test_update_description"
        )

        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circlePort.findByName("test2") >> Optional.ofNullable(this.mockCircleDomainModel)

        when:
        this.circleService.update("test", "test", mockCircleUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Circle update unauthorized api call"() {
        given:
        def mockCircleUpdateRequestDto = new CircleUpdateRequestDto(
                "test2",
                "/test",
                "test_update_description"
        )

        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circlePort.findByName("test2") >> Optional.ofNullable(null)

        when: "not leader"
        def mockApiCallUser = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.LEADER_CIRCLE,
                null,
                UserState.ACTIVE
        )
        this.userPort.findById("test1") >> Optional.of(mockApiCallUser)

        this.circleService.update("test1", "test", mockCircleUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "unauthorized role"
        this.leader.setRole(Role.COMMON)
        this.circleService.update("test", "test", mockCircleUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Circle update already deleted"() {
        given:
        def mockCircleUpdateRequestDto = new CircleUpdateRequestDto(
                "test2",
                "/test",
                "test_update_description"
        )

        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circlePort.findByName("test2") >> Optional.ofNullable(null)

        when:
        ((CircleDomainModel)this.mockCircleDomainModel).setIsDeleted(true)
        this.circleService.update("test", "test", mockCircleUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Circle update invalid parameter"() {
        given:
        def mockCircleUpdateRequestDto = new CircleUpdateRequestDto(
                "",
                "/test",
                "test_update_description"
        )

        def mockUpdatedCircleDomainModel = CircleDomainModel.of(
                (String) this.mockCircleDomainModel.getId(),
                mockCircleUpdateRequestDto.getName(),
                mockCircleUpdateRequestDto.getMainImage(),
                mockCircleUpdateRequestDto.getDescription(),
                (Boolean) this.mockCircleDomainModel.getIsDeleted(),
                (UserDomainModel) this.mockCircleDomainModel.getLeader().orElse(null)
        )

        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circlePort.findByName("") >> Optional.ofNullable(null)

        PowerMockito.mockStatic(CircleDomainModel.class)

        when: "name is blank"
        PowerMockito.when(CircleDomainModel.of(
                (String) this.mockCircleDomainModel.getId(),
                mockCircleUpdateRequestDto.getName(),
                mockCircleUpdateRequestDto.getMainImage(),
                mockCircleUpdateRequestDto.getDescription(),
                (Boolean) this.mockCircleDomainModel.getIsDeleted(),
                (UserDomainModel) this.mockCircleDomainModel.getLeader().orElse(null)
        )).thenReturn(mockUpdatedCircleDomainModel)

        this.circleService.update("test", "test", mockCircleUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    /**
     * Test cases for circle delete
     */
    @Test
    def "Circle delete normal case"() {
        given:
        def mockDeletedCircleDomainModel = CircleDomainModel.of(
                (String) this.mockCircleDomainModel.getId(),
                (String) this.mockCircleDomainModel.getName(),
                (String) this.mockCircleDomainModel.getMainImage(),
                (String) this.mockCircleDomainModel.getDescription(),
                true,
                (UserDomainModel) this.mockCircleDomainModel.getLeader().orElse(null)
        )

        this.userPort.findById("test") >> Optional.of(this.leader)
        this.userPort.updateRole(((UserDomainModel)this.leader).getId(), Role.COMMON) >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circlePort.delete("test") >> Optional.of(mockDeletedCircleDomainModel)

        when:
        def circleResponseDto = this.circleService.delete("test", "test")

        then:
        circleResponseDto instanceof CircleResponseDto
        with(circleResponseDto) {
            getIsDeleted()
        }
    }

    @Test
    def "Circle delete unauthorized api call"() {
        given:
        def mockApiCallUser = UserDomainModel.of(
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

        this.userPort.findById("test") >> Optional.of(this.leader)
        this.userPort.findById("test1") >> Optional.of(mockApiCallUser)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)

        when: "not leader"
        this.circleService.delete("test1", "test")

        then:
        thrown(UnauthorizedException)

        when: "unauthorized role"
        this.leader.setRole(Role.COMMON)
        this.circleService.delete("test", "test")

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Circle delete already deleted"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)

        when:
        this.mockCircleDomainModel.setIsDeleted(true)
        this.circleService.delete("test", "test")

        then:
        thrown(BadRequestException)
    }

    /**
     * Test cases for user circle apply
     */
    @Test
    def "Circle user apply normal case"() {
        given:
        def circleMember = UserDomainModel.of(
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

        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.userPort.findById("test1") >> Optional.of(circleMember)

        when: "new apply"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findByUserIdAndCircleId("test1", "test") >> Optional.ofNullable(null)
        this.circleMemberPort.create(circleMember, (CircleDomainModel) this.mockCircleDomainModel) >> this.mockCircleMemberDomainModel
        def applyUser = this.circleService.userApply("test1", "test")

        then:
        applyUser instanceof CircleMemberResponseDto
        with(applyUser) {
            getUser().getId() == "test1"
            getCircle().getId() == "test"
        }

        when: "update apply"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.LEAVE)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.ofNullable(this.mockCircleMemberDomainModel)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.AWAIT) >> Optional.of(this.mockCircleMemberDomainModel)

        applyUser = this.circleService.userApply("test", "test")

        then:
        applyUser instanceof CircleMemberResponseDto
        with(applyUser) {
            getUser().getId() == "test"
            getCircle().getId() == "test"
        }
    }

    @Test
    def "Circle user apply circle deleted"() {
        given:
        this.mockCircleDomainModel.setIsDeleted(true)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.userPort.findById("test") >> Optional.of(this.leader)

        when:
        this.circleService.userApply("test", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Circle user apply invalid student id"() {
        given:
        this.leader.setStudentId(null)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.userPort.findById("test") >> Optional.of(this.leader)

        when:
        this.circleService.userApply("test", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Circle user apply already member"() {
        given:
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)

        when:
        this.circleService.userApply("test", "test")

        then:
        thrown(BadRequestException)
    }

    /**
     * Test cases for leave user
     */
    @Test
    def "Circle leave user normal case"() {
        given:
        def mockApiCallUser = UserDomainModel.of(
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

        this.userPort.findById("test1") >> Optional.of(mockApiCallUser)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test1", "test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.LEAVE) >> Optional.of(this.mockCircleMemberDomainModel)

        when:
        def leaveUser = this.circleService.leaveUser("test1", "test")

        then:
        leaveUser instanceof CircleMemberResponseDto
        leaveUser.getId() == "test"
    }

    @Test
    def "Circle leave user deleted circle"() {
        given:
        this.mockCircleDomainModel.setIsDeleted(true)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)

        when:
        this.circleService.leaveUser("test", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Circle leave user invalid case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)

        when: "Test with invalid user circle status"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleService.leaveUser("test", "test")

        then:
        thrown(BadRequestException)

        when: "Test with leader circle id"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.MEMBER)
        this.circleService.leaveUser("test", "test")

        then:
        thrown(BadRequestException)
    }

    /**
     * Test cases for drop user
     */
    @Test
    def "Circle drop user normal case"() {
        given:
        def mockApiCallUser = UserDomainModel.of(
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

        this.userPort.findById("test") >> Optional.of(this.leader)
        this.userPort.findById("test1") >> Optional.of(mockApiCallUser)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test1", "test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.DROP) >> Optional.of(this.mockCircleMemberDomainModel)

        when: "request user is leader circle"
        def dropUser = this.circleService.dropUser("test", "test1", "test")

        then:
        dropUser instanceof CircleMemberResponseDto
        dropUser.getId() == "test"

        when: "request user is admin"
        this.leader.setRole(Role.ADMIN)
        dropUser = this.circleService.dropUser("test", "test1", "test")

        then:
        dropUser instanceof CircleMemberResponseDto
        dropUser.getId() == "test"
    }

    @Test
    def "Circle drop user deleted circle"() {
        given:
        def mockApiCallUser = UserDomainModel.of(
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

        this.mockCircleDomainModel.setIsDeleted(true)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.userPort.findById("test1") >> Optional.of(mockApiCallUser)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test1", "test") >> Optional.of(this.mockCircleMemberDomainModel)

        when:
        this.circleService.dropUser("test", "test1", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Circle drop user invalid case"() {
        given:
        def mockApiCallUser = UserDomainModel.of(
                "test1",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.LEADER_CIRCLE,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test1") >> Optional.of(mockApiCallUser)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test1", "test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(this.mockCircleMemberDomainModel)

        when: "Test without leader user id at request user id"
        this.circleService.dropUser("test1", "test1", "test")

        then:
        thrown(UnauthorizedException)

        when: "Test with invalid user circle status"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleService.dropUser("test", "test1", "test")

        then:
        thrown(BadRequestException)

        when: "Test with leader circle id case for dropped user"
        this.circleService.dropUser("test", "test", "test")

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
                (CircleDomainModel) this.mockCircleDomainModel,
                "test1",
                "test",
                null,
                null
        )

        this.mockCircleMemberDomainModel.setUserId("test1")
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findById("test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.userPort.findById("test1") >> Optional.of(this.leader)

        this.circleMemberPort.updateStatus("test", CircleMemberStatus.MEMBER) >> Optional.of(updatedCircleMemberDomainModel)
        this.circleMemberPort.updateStatus("test", CircleMemberStatus.REJECT) >> Optional.of(updatedCircleMemberDomainModel)

        when: "Accept user"
        updatedCircleMemberDomainModel.setStatus(CircleMemberStatus.MEMBER)
        def acceptUser = this.circleService.acceptUser("test", "test")

        then:
        acceptUser instanceof CircleMemberResponseDto
        acceptUser.getStatus() == CircleMemberStatus.MEMBER

        when: "Accept user for admin"
        this.leader.setRole(Role.ADMIN)
        acceptUser = this.circleService.acceptUser("test", "test")

        then:
        acceptUser instanceof CircleMemberResponseDto
        acceptUser.getStatus() == CircleMemberStatus.MEMBER

        when: "Reject user"
        this.leader.setRole(Role.LEADER_CIRCLE)
        updatedCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        def rejectUser = this.circleService.rejectUser("test", "test")

        then:
        rejectUser instanceof CircleMemberResponseDto
        rejectUser.getStatus() == CircleMemberStatus.REJECT

        when: "Reject user for admin"
        this.leader.setRole(Role.ADMIN)
        rejectUser = this.circleService.rejectUser("test", "test")

        then:
        rejectUser instanceof CircleMemberResponseDto
        rejectUser.getStatus() == CircleMemberStatus.REJECT
    }

    @Test
    def "Accept & Reject user deleted circle"() {
        given:
        CircleMemberDomainModel updatedCircleMemberDomainModel = CircleMemberDomainModel.of(
                "test",
                CircleMemberStatus.MEMBER,
                (CircleDomainModel) this.mockCircleDomainModel,
                "test1",
                "test",
                null,
                null
        )

        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findById("test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.userPort.findById("test1") >> Optional.of(this.leader)

        when: "Accept user"
        updatedCircleMemberDomainModel.setStatus(CircleMemberStatus.MEMBER)
        this.circleService.acceptUser("test", "test")

        then:
        thrown(BadRequestException)

        when: "Reject user"
        updatedCircleMemberDomainModel.setStatus(CircleMemberStatus.REJECT)
        this.circleService.rejectUser("test", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Accept & Reject user unauthenticated case"() {
        given:
        this.mockCircleMemberDomainModel.setUserId("test1")
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.AWAIT)
        this.circleMemberPort.findById("test") >> Optional.of(this.mockCircleMemberDomainModel)
        this.userPort.findById("invalid-test") >> Optional.of(this.leader)
        this.userPort.findById("test") >> Optional.of(this.leader)
        this.userPort.findById("test1") >> Optional.of(this.leader)

        when: "Accept user invalid role"
        this.leader.setRole(Role.COMMON)
        this.circleService.acceptUser("test", "test")

        then:
        thrown(UnauthorizedException)

        when: "Reject user invalid role"
        this.circleService.rejectUser("test", "test")

        then:
        thrown(UnauthorizedException)

        when: "Accept user with user id who is not a leader"
        this.leader.setRole(Role.LEADER_CIRCLE)
        this.circleService.acceptUser("invalid-test", "test")

        then:
        thrown(UnauthorizedException)

        when: "Reject user with user id who is not a leader"
        this.circleService.rejectUser("invalid-test", "test")

        then:
        thrown(UnauthorizedException)

        when: "Accept user with circle member status is not await"
        this.mockCircleMemberDomainModel.setStatus(CircleMemberStatus.MEMBER)
        this.circleService.acceptUser("test", "test")

        then:
        thrown(BadRequestException)

        when: "Reject user with circle member status is not await"
        this.circleService.rejectUser("test", "test")

        then:
        thrown(BadRequestException)
    }
}
