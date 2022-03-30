package net.causw.application

import net.causw.application.dto.board.BoardCreateRequestDto
import net.causw.application.dto.board.BoardResponseDto
import net.causw.application.dto.board.BoardUpdateRequestDto
import net.causw.application.spi.*
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
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private BoardService boardService = new BoardService(
            this.boardPort,
            this.userPort,
            this.circlePort,
            this.validator
    )

    def mockUserDomainModel
    def mockBoardDomainModel
    def mockCircleDomainModel

    def setup() {
        this.mockUserDomainModel = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.mockBoardDomainModel = BoardDomainModel.of(
                "test",
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                false,
                null
        )

        this.mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                (UserDomainModel) this.mockUserDomainModel
        )
    }

    /**
     * Test cases for board find all
     */
    @Test
    def "Board find all normal case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findAll() >> List.of(this.mockBoardDomainModel)

        when:
        def boardResponseDtoList = this.boardService.findAll("test")

        then:
        boardResponseDtoList instanceof List<BoardResponseDto>
        with(boardResponseDtoList) {
            get(0).getId() == "test"
        }
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
                "test category",
                null
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        PowerMockito.mockStatic(BoardDomainModel.class)

        when: "create board without circle"
        PowerMockito.when(BoardDomainModel.of(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
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
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)

        PowerMockito.when(BoardDomainModel.of(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                (CircleDomainModel) this.mockCircleDomainModel
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)

        boardResponseDto = this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test"
            getDescription() == "test_description"
        }

        when: "create board with circle for admin"
        mockBoardCreateRequestDto.setCircleId("test")
        this.mockUserDomainModel.setId("admin-test")
        this.mockUserDomainModel.setRole(Role.ADMIN)

        PowerMockito.when(BoardDomainModel.of(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                (CircleDomainModel) this.mockCircleDomainModel
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)

        boardResponseDto = this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test"
            getDescription() == "test_description"
        }
    }

    @Test
    def "Board create invalid parameter"() {
        given:
        def mockBoardCreateRequestDto = new BoardCreateRequestDto(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                null
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        PowerMockito.mockStatic(BoardDomainModel.class)

        when: "name is blank"
        mockBoardCreateRequestDto.setName("")
        this.mockBoardDomainModel.setName("")
        PowerMockito.when(BoardDomainModel.of(
                "",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
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
        PowerMockito.when(BoardDomainModel.of(
                "test",
                "test_description",
                null,
                "test category",
                null
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    @Test
    def "Board create unauthorized case"() {
        given:
        def mockBoardCreateRequestDto = new BoardCreateRequestDto(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                "test"
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        when: "invalid leader id"
        this.mockUserDomainModel.setId("invalid_test")
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "invalid leader role"
        this.mockUserDomainModel.setId("test")
        this.mockUserDomainModel.setRole(Role.PRESIDENT)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "invalid request user role"
        mockBoardCreateRequestDto.setCircleId(null)
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Board create deleted circle"() {
        given:
        def mockBoardCreateRequestDto = new BoardCreateRequestDto(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                "test"
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        when: "invalid leader id"
        this.mockCircleDomainModel.setIsDeleted(true)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(BadRequestException)
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
                "test category"
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)
        this.boardPort.update("test", (BoardDomainModel)this.mockBoardDomainModel) >> Optional.of(this.mockBoardDomainModel)

        when: "update board without circle"
        def boardResponseDto = this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test_update"
            getDescription() == "test_description"
        }

        when: "update board with circle"
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        this.mockBoardDomainModel.setCircle(this.mockCircleDomainModel)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        boardResponseDto = this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test_update"
            getDescription() == "test_description"
        }
    }

    @Test
    def "Board update deleted case"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category"
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "board already deleted"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(BadRequestException)

        when: "circle already deleted"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockBoardDomainModel.setCircle(this.mockCircleDomainModel)
        this.mockCircleDomainModel.setIsDeleted(true)
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
                "test category"
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "name is blank"
        mockBoardUpdateRequestDto.setName("")
        this.mockBoardDomainModel.setName("")
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "create role is null"
        mockBoardUpdateRequestDto.setName("test")
        this.mockBoardDomainModel.setName("test")
        mockBoardUpdateRequestDto.setCreateRoleList(null)
        this.mockBoardDomainModel.setCreateRoleList(null)
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    @Test
    def "Board update unauthorized case"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category"
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "invalid updater role"
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "invalid updater role with circle"
        this.mockUserDomainModel.setRole(Role.PRESIDENT)
        this.mockBoardDomainModel.setCircle(this.mockCircleDomainModel)
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(UnauthorizedException)

        when: "invalid leader"
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        this.mockUserDomainModel.setId("invalid-test")
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
        def mockDeletedBoardDomainModel = BoardDomainModel.of(
                (String) this.mockBoardDomainModel.getId(),
                (String) this.mockBoardDomainModel.getName(),
                (String) this.mockBoardDomainModel.getDescription(),
                (List<String>) this.mockBoardDomainModel.getCreateRoleList(),
                "test category",
                true,
                null
        )

        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
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
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        this.circlePort.findById("test") >> Optional.of(this.mockCircleDomainModel)
        boardResponseDto = this.boardService.delete("test", "test")

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getIsDeleted()
        }
    }

    @Test
    def "Board delete deleted case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "board already delete"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.boardService.delete("test", "test")

        then:
        thrown(BadRequestException)

        when: "circle already delete"
        this.mockBoardDomainModel.setIsDeleted(false)
        this.mockBoardDomainModel.setCircle(this.mockCircleDomainModel)
        this.mockCircleDomainModel.setIsDeleted(true)
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        this.boardService.delete("test", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Board delete unauthorized case"() {
        given:
        this.userPort.findById("test") >> Optional.of(this.mockUserDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "invalid deleter role"
        this.mockUserDomainModel.setRole(Role.COMMON)
        this.boardService.delete("test", "test")

        then:
        thrown(UnauthorizedException)

        when: "invalid updater role with circle"
        this.mockUserDomainModel.setRole(Role.PRESIDENT)
        this.mockBoardDomainModel.setCircle(this.mockCircleDomainModel)
        this.boardService.delete("test", "test")

        then:
        thrown(UnauthorizedException)

        when: "invalid leader"
        this.mockUserDomainModel.setRole(Role.LEADER_CIRCLE)
        this.mockUserDomainModel.setId("invalid-test")
        this.boardService.delete("test", "test")

        then:
        thrown(UnauthorizedException)
    }
}
