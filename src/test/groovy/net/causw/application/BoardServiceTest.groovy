package net.causw.application

import net.causw.application.dto.BoardCreateRequestDto
import net.causw.application.dto.BoardResponseDto
import net.causw.application.dto.BoardUpdateRequestDto
import net.causw.application.spi.BoardPort
import net.causw.application.spi.CircleMemberPort
import net.causw.application.spi.CirclePort
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
@PrepareForTest([BoardDomainModel.class])
class BoardServiceTest extends Specification {
    private BoardPort boardPort = Mock(BoardPort.class)
    private UserPort userPort = Mock(UserPort.class)
    private CirclePort circlePort = Mock(CirclePort.class)
    private CircleMemberPort circleMemberPort = Mock(CircleMemberPort.class)
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private BoardService boardService = new BoardService(this.boardPort, this.userPort, this.circlePort, this.circleMemberPort, this.validator)

    def mockBoardDomainModel

    def setup() {
        this.mockBoardDomainModel = BoardDomainModel.of(
                "test",
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                false,
                null
        )
    }

    /**
     * Test cases for board of circle find all
     */
    @Test
    def "Board find by circle normal case"() {
        given:
        def leader = UserDomainModel.of(
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
        this.mockBoardDomainModel.setCircle(circle)

        this.userPort.findById("test") >> Optional.of(leader)
        this.circlePort.findById("test") >> Optional.of(circle)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(circleMember)
        this.boardPort.findByCircleId("test") >> List.of(this.mockBoardDomainModel)

        when:
        def boardResponseDtoList = this.boardService.findByCircleId("test", "test")

        then:
        boardResponseDtoList instanceof List<BoardResponseDto>
        with(boardResponseDtoList) {
            get(0).getCircleId() == "test"
        }
    }

    @Test
    def "Board find by circle circle already deleted"() {
        given:
        def leader = UserDomainModel.of(
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

        def circle = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                true,
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
        this.mockBoardDomainModel.setCircle(circle)

        this.userPort.findById("test") >> Optional.of(leader)
        this.circlePort.findById("test") >> Optional.of(circle)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(circleMember)
        this.boardPort.findByCircleId("test") >> List.of(this.mockBoardDomainModel)

        when:
        this.boardService.findByCircleId("test", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Board find by circle invalid circle member state"() {
        given:
        def leader = UserDomainModel.of(
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
        this.mockBoardDomainModel.setCircle(circle)

        this.userPort.findById("test") >> Optional.of(leader)
        this.circlePort.findById("test") >> Optional.of(circle)
        this.circleMemberPort.findByUserIdAndCircleId("test", "test") >> Optional.of(circleMember)
        this.boardPort.findByCircleId("test") >> List.of(this.mockBoardDomainModel)

        when: "Circle member is await"
        circleMember.setStatus(CircleMemberStatus.AWAIT)
        this.boardService.findByCircleId("test", "test")

        then:
        thrown(BadRequestException)

        when: "Circle member is drop"
        circleMember.setStatus(CircleMemberStatus.DROP)
        this.boardService.findByCircleId("test", "test")

        then:
        thrown(UnauthorizedException)

        when: "Circle member is leave"
        circleMember.setStatus(CircleMemberStatus.LEAVE)
        this.boardService.findByCircleId("test", "test")

        then:
        thrown(BadRequestException)

        when: "Circle member is reject"
        circleMember.setStatus(CircleMemberStatus.REJECT)
        this.boardService.findByCircleId("test", "test")

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for board create
     */
    @Test
    def "Board create normal case"() {
        given:
        def mockBoardCreateRequestDto = new BoardCreateRequestDto(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                null
        )

        def creatorUserDomainModel = UserDomainModel.of(
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

        def circleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                creatorUserDomainModel
        )

        this.userPort.findById("test") >> Optional.of(creatorUserDomainModel)
        this.circlePort.findById("test") >> Optional.of(circleDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        when: "create board without circle"
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                null
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        def boardResponseDto = this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test"
            getDescription() == "test_description"
        }

        when: "create board with circle"
        mockBoardCreateRequestDto.setCircleId("test")
        creatorUserDomainModel.setRole(Role.LEADER_CIRCLE)
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                circleDomainModel
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        boardResponseDto = this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test"
            getDescription() == "test_description"
        }
    }

    @Test
    def "Board create invalid data"() {
        given:
        def mockBoardCreateRequestDto = new BoardCreateRequestDto(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                null
        )

        def mockCreatorDomainModel = UserDomainModel.of(
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

        this.userPort.findById("test") >> Optional.of(mockCreatorDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        when: "name is blank"
        mockBoardCreateRequestDto.setName("")
        this.mockBoardDomainModel.setName("")
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                null
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "create role is null"
        mockBoardCreateRequestDto.setName("test")
        this.mockBoardDomainModel.setName("test")
        mockBoardCreateRequestDto.setCreateRoleList(null)
        this.mockBoardDomainModel.setCreateRoleList(null)
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "test",
                "test_description",
                null,
                null
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    @Test
    def "Board create invalid role"() {
        given:
        def mockBoardCreateRequestDto = new BoardCreateRequestDto(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                null
        )

        def mockCreatorDomainModel = UserDomainModel.of(
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

        this.userPort.findById("test") >> Optional.of(mockCreatorDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        when: "invalid creator role"
        mockCreatorDomainModel.setRole(Role.NONE)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Board create invalid leader"() {
        given:
        def mockBoardCreateRequestDto = new BoardCreateRequestDto(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test"
        )

        def creator = UserDomainModel.of(
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

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                creator
        )

        this.userPort.findById("test") >> Optional.of(creator)
        this.circlePort.findById("test") >> Optional.of(mockCircleDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        when: "invalid leader id"
        creator.setId("invalid_test")
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for board update
     */
    @Test
    def "Board update normal case"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL")
        )

        def updater = UserDomainModel.of(
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

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                updater
        )

        def mockUpdatedBoardDomainModel = BoardDomainModel.of(
                "test",
                mockBoardUpdateRequestDto.getName(),
                mockBoardUpdateRequestDto.getDescription(),
                mockBoardUpdateRequestDto.getCreateRoleList(),
                false,
                null
        )

        this.userPort.findById("test") >> Optional.of(updater)
        this.circlePort.findById("test") >> Optional.of(mockCircleDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)
        this.boardPort.update("test", mockUpdatedBoardDomainModel) >> Optional.of(mockUpdatedBoardDomainModel)

        when: "update board without circle"
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "test",
                mockBoardUpdateRequestDto.getName(),
                mockBoardUpdateRequestDto.getDescription(),
                mockBoardUpdateRequestDto.getCreateRoleList(),
                false,
                null
        )).thenReturn(mockUpdatedBoardDomainModel)
        def boardResponseDto = this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test_update"
            getDescription() == "test_description"
        }

        when: "update board with circle"
        this.mockBoardDomainModel.setCircle(mockCircleDomainModel)
        mockUpdatedBoardDomainModel.setCircle(mockCircleDomainModel)
        updater.setRole(Role.LEADER_CIRCLE)
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "test",
                mockBoardUpdateRequestDto.getName(),
                mockBoardUpdateRequestDto.getDescription(),
                mockBoardUpdateRequestDto.getCreateRoleList(),
                false,
                mockCircleDomainModel
        )).thenReturn(mockUpdatedBoardDomainModel)
        boardResponseDto = this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test_update"
            getDescription() == "test_description"
        }
    }

    @Test
    def "Board update already deleted"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL")
        )

        def updater = UserDomainModel.of(
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

        this.userPort.findById("test") >> Optional.of(updater)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "board already delete"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Board update invalid data"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL")
        )

        def updater = UserDomainModel.of(
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

        this.userPort.findById("test") >> Optional.of(updater)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "name is blank"
        mockBoardUpdateRequestDto.setName("")
        this.mockBoardDomainModel.setName("")
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "test",
                mockBoardUpdateRequestDto.getName(),
                mockBoardUpdateRequestDto.getDescription(),
                mockBoardUpdateRequestDto.getCreateRoleList(),
                false,
                null
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "create role is null"
        mockBoardUpdateRequestDto.setName("test")
        this.mockBoardDomainModel.setName("test")
        mockBoardUpdateRequestDto.setCreateRoleList(null)
        this.mockBoardDomainModel.setCreateRoleList(null)
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "test",
                mockBoardUpdateRequestDto.getName(),
                mockBoardUpdateRequestDto.getDescription(),
                mockBoardUpdateRequestDto.getCreateRoleList(),
                false,
                null
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    @Test
    def "Board update invalid role"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL")
        )

        def updater = UserDomainModel.of(
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

        this.userPort.findById("test") >> Optional.of(updater)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "invalid creator role"
        updater.setRole(Role.NONE)
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Board update invalid leader"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL"),
                Arrays.asList("PRESIDENT", "COUNCIL")
        )

        def updater = UserDomainModel.of(
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

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                updater
        )

        this.mockBoardDomainModel.setCircle(mockCircleDomainModel)
        this.userPort.findById("test") >> Optional.of(updater)
        this.circlePort.findById("test") >> Optional.of(mockCircleDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "invalid leader id"
        updater.setId("invalid_test")
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for board delete
     */
    @Test
    def "Board delete normal case"() {
        given:
        def deleter = UserDomainModel.of(
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

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                deleter
        )

        def mockDeletedBoardDomainModel = BoardDomainModel.of(
                (String) this.mockBoardDomainModel.getId(),
                (String) this.mockBoardDomainModel.getName(),
                (String) this.mockBoardDomainModel.getDescription(),
                (List<String>) this.mockBoardDomainModel.getCreateRoleList(),
                true,
                mockCircleDomainModel
        )

        this.userPort.findById("test") >> Optional.of(deleter)
        this.circlePort.findById("test") >> Optional.of(mockCircleDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)
        this.boardPort.delete("test") >> Optional.of(mockDeletedBoardDomainModel)

        when: "update board without circle"
        def boardResponseDto = this.boardService.delete("test", "test")

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getIsDeleted()
        }

        when: "update board with circle"
        this.mockBoardDomainModel.setCircle(mockCircleDomainModel)
        mockDeletedBoardDomainModel.setCircle(mockCircleDomainModel)
        deleter.setRole(Role.LEADER_CIRCLE)
        boardResponseDto = this.boardService.delete("test", "test")

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getIsDeleted()
        }
    }

    @Test
    def "Board delete already deleted"() {
        given:
        def deleter = UserDomainModel.of(
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

        this.userPort.findById("test") >> Optional.of(deleter)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "board already delete"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.boardService.delete("test", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Board delete invalid role"() {
        given:
        def deleter = UserDomainModel.of(
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

        this.userPort.findById("test") >> Optional.of(deleter)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "invalid creator role"
        deleter.setRole(Role.NONE)
        this.boardService.delete("test", "test")

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Board delete invalid leader"() {
        given:
        def deleter = UserDomainModel.of(
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

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                deleter
        )

        this.mockBoardDomainModel.setCircle(mockCircleDomainModel)
        this.userPort.findById("test") >> Optional.of(deleter)
        this.circlePort.findById("test") >> Optional.of(mockCircleDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "invalid leader id"
        deleter.setId("invalid_test")
        this.boardService.delete("test", "test")

        then:
        thrown(UnauthorizedException)
    }
}
