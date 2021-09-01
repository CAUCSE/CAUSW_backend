package net.causw.application

import net.causw.adapter.persistence.Board
import net.causw.adapter.persistence.Circle
import net.causw.adapter.persistence.User
import net.causw.application.dto.BoardCreateRequestDto
import net.causw.application.dto.BoardResponseDto
import net.causw.application.dto.CircleFullDto
import net.causw.application.dto.UserFullDto
import net.causw.application.spi.BoardPort
import net.causw.application.spi.CirclePort
import net.causw.application.spi.UserPort
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
class BoardServiceTest extends Specification {
    private BoardPort boardPort = Mock(BoardPort.class)
    private UserPort userPort = Mock(UserPort.class)
    private CirclePort circlePort = Mock(CirclePort.class)
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private BoardService boardService = new BoardService(this.boardPort, this.userPort, this.circlePort, this.validator)

    def id
    def mockBoard

    def setup() {
        id = "test"
        mockBoard = Board.of(
                "test",
                "test_description",
                "PRESIDENT,COUNCIL",
                "PRESIDENT,COUNCIL",
                "PRESIDENT,COUNCIL",
                false,
                null
        )
    }

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

        def mockBoardResponseDto = BoardResponseDto.from((Board)mockBoard)

        def creator = User.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                UserState.WAIT
        )
        def mockCreatorFullDto = UserFullDto.from(creator)

        def mockCircleFullDto = CircleFullDto.from(Circle.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                creator
        ))

        this.userPort.findById((String)id) >> Optional.of(mockCreatorFullDto)
        this.circlePort.findById((String)id) >> Optional.of(mockCircleFullDto)
        this.boardPort.create(mockBoardCreateRequestDto, Optional.ofNullable(null)) >> mockBoardResponseDto
        this.boardPort.create(mockBoardCreateRequestDto, Optional.ofNullable(mockCircleFullDto)) >> mockBoardResponseDto

        when: "create board without circle"
        def boardResponseDto = this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test"
            getDescription() == "test_description"
        }

        when: "create board with circle"
        mockBoardCreateRequestDto.setCircleId("test")
        mockCreatorFullDto.setRole(Role.LEADER_CIRCLE)
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
        def mockBoardResponseDto = BoardResponseDto.from((Board)mockBoard)

        def mockCreatorFullDto = UserFullDto.from(User.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                UserState.WAIT
        ))

        this.userPort.findById((String)id) >> Optional.of(mockCreatorFullDto)
        this.boardPort.create(mockBoardCreateRequestDto, null) >> mockBoardResponseDto

        when: "name is blank"
        mockBoardCreateRequestDto.setName("")
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "create role is null"
        mockBoardCreateRequestDto.setName("test")
        mockBoardCreateRequestDto.setCreateRoleList(null)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "modify role is null"
        mockBoardCreateRequestDto.setCreateRoleList(Arrays.asList("PRESIDENT", "COUNCIL"))
        mockBoardCreateRequestDto.setModifyRoleList(null)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "read role is null"
        mockBoardCreateRequestDto.setModifyRoleList(Arrays.asList("PRESIDENT", "COUNCIL"))
        mockBoardCreateRequestDto.setReadRoleList(null)
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
        def mockBoardResponseDto = BoardResponseDto.from((Board)mockBoard)

        def mockCreatorFullDto = UserFullDto.from(User.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                UserState.WAIT
        ))

        this.userPort.findById((String)id) >> Optional.of(mockCreatorFullDto)
        this.boardPort.create(mockBoardCreateRequestDto, null) >> mockBoardResponseDto

        when: "invalid creator role"
        mockCreatorFullDto.setRole(Role.NONE)
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

        def mockBoardResponseDto = BoardResponseDto.from((Board)mockBoard)

        def creator = User.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                UserState.WAIT
        )
        def mockCreatorFullDto = UserFullDto.from(creator)

        def mockCircleFullDto = CircleFullDto.from(Circle.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                creator
        ))

        this.userPort.findById((String)id) >> Optional.of(mockCreatorFullDto)
        this.circlePort.findById((String)id) >> Optional.of(mockCircleFullDto)
        this.boardPort.create(mockBoardCreateRequestDto, Optional.ofNullable(mockCircleFullDto)) >> mockBoardResponseDto

        when: "invalid leader id"
        mockCreatorFullDto.setId("invalid_test")
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

}
