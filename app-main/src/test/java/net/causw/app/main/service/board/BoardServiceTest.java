package net.causw.app.main.service.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

import java.util.List;

import java.util.Optional;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.notification.UserBoardSubscribe;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.dto.board.BoardCreateRequestDto;
import net.causw.app.main.dto.board.BoardResponseDto;
import net.causw.app.main.repository.board.BoardRepository;
import net.causw.app.main.repository.notification.UserBoardSubscribeRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.util.ObjectFixtures;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BoardServiceTest {

  @InjectMocks
  private BoardService boardService;

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
    void createNoticeBoardDuplicateName() {
      given(boardRepository.existsByName(boardCreateRequestDto.getBoardName())).willReturn(true);

      assertThrows(BadRequestException.class,
          () -> boardService.createNoticeBoard(creator, boardCreateRequestDto));

      then(boardRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("게시판 카테고리가 앱 공지가 아니면 예외 발생")
    void createNoticeBoardInvalidCategory() {
      given(boardRepository.existsByName(boardCreateRequestDto.getBoardName())).willReturn(false);

      assertThrows(BadRequestException.class,
          () -> boardService.createNoticeBoard(creator, boardCreateRequestDto));

      then(boardRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("정상 생성 시 게시판 저장 및 구독 생성 호출")
    void createNoticeBoardSuccess() {
      Board savedBoard = ObjectFixtures.getNoticeBoard(true);

      given(boardRepository.existsByName(noticeBoardCreateRequestDto.getBoardName())).willReturn(false);
      given(boardRepository.findById(savedBoard.getId())).willReturn(Optional.of(savedBoard));
      given(boardRepository.save(any(Board.class))).willReturn(savedBoard);

      BoardResponseDto response = boardService.createNoticeBoard(creator, noticeBoardCreateRequestDto);

      assertThat(response.getName()).isEqualTo(noticeBoardCreateRequestDto.getBoardName());
      then(boardRepository).should().save(any(Board.class));
    }
  }

  @Nested
  @DisplayName("게시판 구독 생성")
  class getBoardSubscribeTest {

    private static final Board board = ObjectFixtures.getBoard();

    @Test
    @DisplayName("활성 사용자 없으면 구독 생성 안 함")
    void getBoardSubscribeNoActiveUsers() {
      given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
      given(userRepository.findAllByState(any())).willReturn(List.of());

      boardService.createBoardSubscribe(board.getId());

      then(userBoardSubscribeRepository).should().saveAll(List.of());
    }

    @Test
    @DisplayName("동문회 허용 게시판이면 졸업자 포함 구독 생성")
    void getBoardSubscribeIncludeGraduated() {
      Board board = ObjectFixtures.getNoticeBoard(true);

      User graduatedUser = ObjectFixtures.getCertifiedUser();
      graduatedUser.setAcademicStatus(AcademicStatus.GRADUATED);

      User activeUser = ObjectFixtures.getCertifiedUser();

      given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
      given(userRepository.findAllByState(any())).willReturn(List.of(graduatedUser, activeUser));

      boardService.createBoardSubscribe(board.getId());

      ArgumentCaptor<List<UserBoardSubscribe>> captor = ArgumentCaptor.forClass(List.class);
      then(userBoardSubscribeRepository).should().saveAll(captor.capture());

      assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("동문회 미허용 게시판이면 졸업자 제외하고 구독 생성")
    void getBoardSubscribeExcludeGraduated() {
      Board board = ObjectFixtures.getNoticeBoard(false);

      User graduatedUser = ObjectFixtures.getCertifiedUser();
      graduatedUser.setAcademicStatus(AcademicStatus.GRADUATED);

      User activeUser = ObjectFixtures.getCertifiedUser();

      given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
      given(userRepository.findAllByState(any())).willReturn(List.of(graduatedUser, activeUser));

      boardService.createBoardSubscribe(board.getId());

      ArgumentCaptor<List<UserBoardSubscribe>> captor = ArgumentCaptor.forClass(List.class);
      then(userBoardSubscribeRepository).should().saveAll(captor.capture());

      assertThat(captor.getValue()).hasSize(1);
    }
  }
}

