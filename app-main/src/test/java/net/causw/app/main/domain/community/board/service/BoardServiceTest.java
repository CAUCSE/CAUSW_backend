package net.causw.app.main.domain.community.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.board.service.dto.mapper.BoardConfigDetailMapper;
import net.causw.app.main.domain.community.board.service.dto.mapper.BoardConfigPartMapper;
import net.causw.app.main.domain.community.board.service.dto.mapper.BoardConfigSummaryMapper;
import net.causw.app.main.domain.community.board.service.dto.mapper.BoardPartMapper;
import net.causw.app.main.domain.community.board.service.dto.request.BoardConfigPart;
import net.causw.app.main.domain.community.board.service.dto.request.BoardOrderUpdateCommand;
import net.causw.app.main.domain.community.board.service.dto.request.BoardPart;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigDetail;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigListResult;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigSummary;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigWriter;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardWriter;
import net.causw.app.main.domain.community.board.util.BoardValidator;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.v2.implementation.UserReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

	@InjectMocks
	private BoardAdminService boardService;

	@Mock
	private BoardReader boardReader;
	@Mock
	private BoardConfigReader boardConfigReader;
	@Mock
	private BoardWriter boardWriter;
	@Mock
	private BoardConfigWriter boardConfigWriter;
	@Mock
	private UserReader userReader;
	@Mock
	private BoardConfigDetailMapper boardConfigDetailMapper;
	@Mock
	private BoardConfigSummaryMapper boardConfigSummaryMapper;
	@Mock
	private BoardPartMapper boardPartMapper;
	@Mock
	private BoardConfigPartMapper boardConfigPartMapper;
	@Mock
	private BoardValidator boardValidator;

	@Nested
	@DisplayName("getAllBoardList")
	class GetAllBoardListTest {

		@Test
		@DisplayName("조건에 맞는 게시판 목록과 설정을 조회해 BoardConfigListResult로 반환한다")
		void givenQueryCondition_whenGetAllBoardList_thenReturnsBoardConfigListResult() {
			// given
			BoardQueryCondition condition = new BoardQueryCondition(null, null, null, null, null);
			Board board = ObjectFixtures.getBoardWithId("board-1");
			List<Board> boards = List.of(board);
			BoardConfig config = BoardConfig.of("board-1", false, BoardReadScope.BOTH, BoardWriteScope.ALL_USER,
				false, BoardVisibility.VISIBLE, 10);
			Map<String, BoardConfig> configMap = Map.of("board-1", config);
			BoardConfigSummary summary = BoardConfigSummary.builder()
				.no(1L)
				.boardId("board-1")
				.name("boardName")
				.description("boardDescription")
				.isAnonymous(false)
				.readScope(BoardReadScope.BOTH)
				.writeScope(BoardWriteScope.ALL_USER)
				.isNotice(false)
				.visibility(BoardVisibility.VISIBLE)
				.displayOrder(10)
				.build();
			List<BoardConfigSummary> summaries = List.of(summary);
			given(boardReader.searchBoardList(condition)).willReturn(boards);
			given(boardConfigReader.getBoardConfigMapByBoardIds(List.of("board-1"))).willReturn(configMap);
			given(boardConfigSummaryMapper.toSummaries(boards, configMap)).willReturn(summaries);

			// when
			BoardConfigListResult result = boardService.getAllBoardList(condition);

			// then
			assertThat(result.boards()).isEqualTo(summaries);
			then(boardReader).should().searchBoardList(condition);
			then(boardConfigReader).should().getBoardConfigMapByBoardIds(List.of("board-1"));
			then(boardConfigSummaryMapper).should().toSummaries(boards, configMap);
		}
	}

	@Nested
	@DisplayName("getBoardConfigEditInfo")
	class GetBoardConfigEditInfoTest {

		@Test
		@DisplayName("boardId로 게시판·설정·관리자 목록을 조회해 BoardConfigDetail로 반환한다")
		void givenBoardId_whenGetBoardConfigEditInfo_thenReturnsBoardConfigDetail() {
			// given
			String boardId = "board-1";
			Board board = ObjectFixtures.getBoardWithId(boardId);
			BoardConfig boardConfig = BoardConfig.of(boardId, false, BoardReadScope.BOTH, BoardWriteScope.ALL_USER,
				false, BoardVisibility.VISIBLE, 10);
			List<String> adminIds = List.of("user-1");
			List<User> adminUsers = List.of(ObjectFixtures.getCertifiedUser());
			BoardConfigDetail detail = BoardConfigDetail.builder()
				.boardId(boardId)
				.name("boardName")
				.description("boardDescription")
				.isAnonymous(false)
				.readScope(BoardReadScope.BOTH)
				.writeScope(BoardWriteScope.ALL_USER)
				.isNotice(false)
				.visibility(BoardVisibility.VISIBLE)
				.displayOrder(10)
				.admins(List.of())
				.build();
			given(boardReader.getById(boardId)).willReturn(board);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(adminIds);
			given(userReader.getUsersByIds(adminIds)).willReturn(adminUsers);
			given(boardConfigDetailMapper.fromEntity(board, boardConfig, adminUsers)).willReturn(detail);

			// when
			BoardConfigDetail result = boardService.getBoardConfigEditInfo(boardId);

			// then
			assertThat(result).isEqualTo(detail);
			then(boardReader).should().getById(boardId);
			then(boardConfigReader).should().getByBoardId(boardId);
			then(boardConfigReader).should().getAdminIdsByBoardId(boardId);
			then(userReader).should().getUsersByIds(adminIds);
			then(boardConfigDetailMapper).should().fromEntity(board, boardConfig, adminUsers);
		}
	}

	@Nested
	@DisplayName("createBoard")
	class CreateBoardTest {

		private static final BoardPart BOARD_PART = BoardPart.builder()
			.name("newBoard")
			.description("desc")
			.build();
		private static final BoardConfigPart CONFIG_PART = BoardConfigPart.builder()
			.isAnonymous(false)
			.readScope(BoardReadScope.BOTH)
			.writeScope(BoardWriteScope.ALL_USER)
			.isNotice(false)
			.visibility(BoardVisibility.VISIBLE)
			.build();
		private static final List<String> ADMIN_IDS = List.of("user-1");

		@Test
		@DisplayName("이름 중복이면 BoardValidator가 예외를 던진다")
		void givenDuplicateBoardName_whenCreateBoard_thenThrowsException() {
			// given
			org.mockito.Mockito.doThrow(new BaseRunTimeV2Exception(BoardErrorCode.BOARD_NAME_DUPLICATE))
				.when(boardValidator).validateForCreate(BOARD_PART.name());

			// when & then
			assertThatThrownBy(() -> boardService.createBoard(BOARD_PART, CONFIG_PART, ADMIN_IDS))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", BoardErrorCode.BOARD_NAME_DUPLICATE);
			then(boardWriter).should(never()).save(any(Board.class));
		}

		@Test
		@DisplayName("정상 시 검증 후 Board·BoardConfig 저장 및 관리자 교체를 호출한다")
		void givenValidBoardAndConfig_whenCreateBoard_thenSavesAndReplacesAdmins() {
			// given
			Board savedBoard = ObjectFixtures.getBoardV2WithId("board-1");
			BoardConfig config = BoardConfig.of("board-1", false, BoardReadScope.BOTH, BoardWriteScope.ALL_USER,
				false, BoardVisibility.VISIBLE, 10);
			given(boardPartMapper.toEntity(BOARD_PART)).willReturn(savedBoard);
			given(boardWriter.save(any(Board.class))).willReturn(savedBoard);
			given(boardConfigReader.getNextDisplayOrder()).willReturn(10);
			given(boardConfigPartMapper.toEntity(eq(CONFIG_PART), eq("board-1"), eq(10))).willReturn(config);

			// when
			boardService.createBoard(BOARD_PART, CONFIG_PART, ADMIN_IDS);

			// then
			then(boardValidator).should().validateForCreate(BOARD_PART.name());
			then(boardWriter).should().save(any(Board.class));
			then(boardConfigReader).should().getNextDisplayOrder();
			then(boardConfigWriter).should().save(config);
			then(boardConfigWriter).should().replaceAdmins("board-1", java.util.Set.of("user-1"));
		}
	}

	@Nested
	@DisplayName("updateBoard")
	class UpdateBoardTest {

		private static final String BOARD_ID = "board-1";
		private static final BoardPart BOARD_PART = BoardPart.builder()
			.name("updatedName")
			.description("updatedDesc")
			.build();
		private static final BoardConfigPart CONFIG_PART = BoardConfigPart.builder()
			.isAnonymous(true)
			.readScope(BoardReadScope.BOTH)
			.writeScope(BoardWriteScope.ONLY_ADMIN)
			.isNotice(false)
			.visibility(BoardVisibility.VISIBLE)
			.build();
		private static final List<String> ADMIN_IDS = List.of("user-1", "user-2");

		@Test
		@DisplayName("이름 중복이면 BoardValidator가 예외를 던진다")
		void givenDuplicateBoardName_whenUpdateBoard_thenThrowsException() {
			// given
			org.mockito.Mockito.doThrow(new BaseRunTimeV2Exception(BoardErrorCode.BOARD_NAME_DUPLICATE))
				.when(boardValidator).validateForUpdate(BOARD_PART.name(), BOARD_ID);

			// when & then
			assertThatThrownBy(() -> boardService.updateBoard(BOARD_ID, BOARD_PART, CONFIG_PART, ADMIN_IDS))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasFieldOrPropertyWithValue("errorCode", BoardErrorCode.BOARD_NAME_DUPLICATE);
			then(boardWriter).should(never()).updateBoard(any(Board.class), any(BoardPart.class));
		}

		@Test
		@DisplayName("정상 시 검증 후 Board·BoardConfig 업데이트 및 관리자 교체를 호출한다")
		void givenBoardIdAndParts_whenUpdateBoard_thenUpdatesAndReplacesAdmins() {
			// given
			Board board = ObjectFixtures.getBoardWithId(BOARD_ID);
			BoardConfig boardConfig = BoardConfig.of(BOARD_ID, false, BoardReadScope.BOTH, BoardWriteScope.ALL_USER,
				false, BoardVisibility.VISIBLE, 10);
			given(boardReader.getById(BOARD_ID)).willReturn(board);
			given(boardConfigReader.getByBoardId(BOARD_ID)).willReturn(boardConfig);

			// when
			boardService.updateBoard(BOARD_ID, BOARD_PART, CONFIG_PART, ADMIN_IDS);

			// then
			then(boardValidator).should().validateForUpdate(BOARD_PART.name(), BOARD_ID);
			then(boardReader).should().getById(BOARD_ID);
			then(boardConfigReader).should().getByBoardId(BOARD_ID);
			then(boardWriter).should().updateBoard(board, BOARD_PART);
			then(boardConfigWriter).should().updateBoardConfig(boardConfig, CONFIG_PART);
			then(boardConfigWriter).should().replaceAdmins(BOARD_ID, java.util.Set.of("user-1", "user-2"));
		}
	}

	@Nested
	@DisplayName("deleteBoard")
	class DeleteBoardTest {

		@Test
		@DisplayName("게시판 조회 후 isDeleted를 true로 설정하고 저장한다")
		void givenBoardId_whenDeleteBoard_thenSoftDeletesBoard() {
			// given
			String boardId = "board-1";
			Board board = ObjectFixtures.getBoardWithId(boardId);
			given(boardReader.getById(boardId)).willReturn(board);
			given(boardWriter.save(board)).willReturn(board);

			// when
			boardService.deleteBoard(boardId);

			// then
			then(boardReader).should().getById(boardId);
			assertThat(board.getIsDeleted()).isTrue();
			then(boardWriter).should().save(board);
		}
	}

	@Nested
	@DisplayName("updateBoardOrder")
	class UpdateBoardOrderTest {

		@Test
		@DisplayName("boardIds 순서대로 displayOrder 갱신을 BoardConfigWriter에 위임한다")
		void givenBoardOrderCommand_whenUpdateBoardOrder_thenDelegatesToBoardConfigWriter() {
			// given
			BoardOrderUpdateCommand command = BoardOrderUpdateCommand.builder()
				.boardIds(List.of("board-1", "board-2"))
				.build();

			// when
			boardService.updateBoardOrder(command);

			// then
			then(boardConfigWriter).should().updateDisplayOrders(command.boardIds());
		}
	}
}
