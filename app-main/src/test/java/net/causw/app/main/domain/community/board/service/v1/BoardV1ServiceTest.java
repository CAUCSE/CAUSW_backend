package net.causw.app.main.domain.community.board.service.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.api.v1.dto.BoardCreateRequestDto;
import net.causw.app.main.domain.community.board.api.v1.dto.BoardResponseDto;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.repository.BoardRepository;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeRepository;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.util.ObjectFixtures;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;

@ExtendWith(MockitoExtension.class)
public class BoardV1ServiceTest {

	@InjectMocks
	private BoardV1Service boardService;

	@Mock
	private BoardRepository boardRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserBoardSubscribeRepository userBoardSubscribeRepository;

	@Nested
	@DisplayName("공지 게시판 생성")
	class CreateNoticeBoardTest {

		private static final User creator = ObjectFixtures.getUser();
		private static final BoardCreateRequestDto boardCreateRequestDto = BoardCreateRequestDto.builder()
			.boardName("boardName")
			.boardCategory(StaticValue.BOARD_NAME_APP_FREE)
			.build();
		private static final BoardCreateRequestDto noticeBoardCreateRequestDto = BoardCreateRequestDto.builder()
			.boardName("noticeBoardName")
			.boardCategory(StaticValue.BOARD_NAME_APP_NOTICE)
			.build();

		@Test
		@DisplayName("중복 게시판 이름이면 예외 발생")
		void givenDuplicateBoardName_whenCreateNoticeBoard_thenThrowsException() {
			// given
			given(boardRepository.existsByName(boardCreateRequestDto.getBoardName())).willReturn(true);

			// when & then
			assertThatThrownBy(() -> boardService.createNoticeBoard(creator, boardCreateRequestDto))
				.isInstanceOf(BadRequestException.class);
			then(boardRepository).should(never()).save(any());
		}

		@Test
		@DisplayName("게시판 카테고리가 앱 공지가 아니면 예외 발생")
		void givenInvalidCategory_whenCreateNoticeBoard_thenThrowsException() {
			// given
			given(boardRepository.existsByName(boardCreateRequestDto.getBoardName())).willReturn(false);

			// when & then
			assertThatThrownBy(() -> boardService.createNoticeBoard(creator, boardCreateRequestDto))
				.isInstanceOf(BadRequestException.class);
			then(boardRepository).should(never()).save(any());
		}

		@Test
		@DisplayName("정상 생성 시 게시판 저장 및 구독 생성 호출")
		void givenValidNoticeBoard_whenCreateNoticeBoard_thenSavesAndReturnsResponse() {
			// given
			Board savedBoard = ObjectFixtures.getNoticeBoardWithId(true, "notice-board-1");
			given(boardRepository.existsByName(noticeBoardCreateRequestDto.getBoardName())).willReturn(false);
			given(boardRepository.findById(savedBoard.getId())).willReturn(Optional.of(savedBoard));
			given(boardRepository.save(any(Board.class))).willReturn(savedBoard);

			// when
			BoardResponseDto response = boardService.createNoticeBoard(creator, noticeBoardCreateRequestDto);

			// then
			assertThat(response.getName()).isEqualTo(noticeBoardCreateRequestDto.getBoardName());
			then(boardRepository).should().save(any(Board.class));
		}
	}

	@Nested
	@DisplayName("게시판 구독 생성")
	class GetBoardSubscribeTest {

		private static final Board board = ObjectFixtures.getBoardWithId("board-1");

		@Test
		@DisplayName("활성 사용자 없으면 구독 생성 안 함")
		void givenNoActiveUsers_whenCreateBoardSubscribe_thenSavesEmptyList() {
			// given
			given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
			given(userRepository.findAllByState(any())).willReturn(List.of());

			// when
			boardService.createBoardSubscribe(board.getId());

			// then
			then(userBoardSubscribeRepository).should().saveAll(List.of());
		}

		@Test
		@DisplayName("동문회 허용 게시판이면 졸업자 포함 구독 생성")
		void givenAlumniAllowedBoardAndUsers_whenCreateBoardSubscribe_thenIncludesGraduated() {
			// given
			Board board = ObjectFixtures.getNoticeBoardWithId(true, "notice-board-1");
			User graduatedUser = ObjectFixtures.getCertifiedUser();
			graduatedUser.setAcademicStatus(AcademicStatus.GRADUATED);
			User activeUser = ObjectFixtures.getCertifiedUser();
			given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
			given(userRepository.findAllByState(any())).willReturn(List.of(graduatedUser, activeUser));

			// when
			boardService.createBoardSubscribe(board.getId());

			// then
			ArgumentCaptor<List<UserBoardSubscribe>> captor = ArgumentCaptor.forClass(List.class);
			then(userBoardSubscribeRepository).should().saveAll(captor.capture());
			assertThat(captor.getValue()).hasSize(2);
		}

		@Test
		@DisplayName("동문회 미허용 게시판이면 졸업자 제외하고 구독 생성")
		void givenAlumniNotAllowedBoardAndUsers_whenCreateBoardSubscribe_thenExcludesGraduated() {
			// given
			Board board = ObjectFixtures.getNoticeBoardWithId(false, "notice-board-2");
			User graduatedUser = ObjectFixtures.getCertifiedUser();
			graduatedUser.setAcademicStatus(AcademicStatus.GRADUATED);
			User activeUser = ObjectFixtures.getCertifiedUser();
			given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
			given(userRepository.findAllByState(any())).willReturn(List.of(graduatedUser, activeUser));

			// when
			boardService.createBoardSubscribe(board.getId());

			// then
			ArgumentCaptor<List<UserBoardSubscribe>> captor = ArgumentCaptor.forClass(List.class);
			then(userBoardSubscribeRepository).should().saveAll(captor.capture());
			assertThat(captor.getValue()).hasSize(1);
		}
	}
}
