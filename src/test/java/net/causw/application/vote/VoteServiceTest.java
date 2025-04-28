package net.causw.application.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.repository.vote.VoteOptionRepository;
import net.causw.adapter.persistence.repository.vote.VoteRecordRepository;
import net.causw.adapter.persistence.repository.vote.VoteRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.vote.Vote;
import net.causw.adapter.persistence.vote.VoteOption;
import net.causw.adapter.persistence.vote.VoteRecord;
import net.causw.application.dto.vote.CastVoteRequestDto;
import net.causw.application.dto.vote.CreateVoteRequestDto;
import net.causw.application.dto.vote.VoteOptionResponseDto;
import net.causw.application.dto.vote.VoteResponseDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.ObjectFixtures;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class VoteServiceTest {

  @InjectMocks
  VoteService voteService;

  @Mock
  VoteOptionRepository voteOptionRepository;
  @Mock
  VoteRepository voteRepository;
  @Mock
  PostRepository postRepository;
  @Mock
  VoteRecordRepository voteRecordRepository;

  User user;
  Post post;
  Board board;
  List<VoteOption> voteOptions;
  Vote vote;

  @BeforeEach
  public void setUp() {
    user = ObjectFixtures.getUser();
    board = ObjectFixtures.getBoard();
    post = ObjectFixtures.getPost(user, board);
    voteOptions = ObjectFixtures.getVoteOptions();
    voteOptions.forEach(
        voteOption -> ReflectionTestUtils.setField(voteOption, "createdAt", LocalDateTime.now()));
    vote = ObjectFixtures.getVote(voteOptions, post);
  }

  @Nested
  @DisplayName("투표 생성 테스트")
  class voteCreateTest {

    CreateVoteRequestDto createVoteRequestDto;

    @BeforeEach
    public void setUp() {
      createVoteRequestDto = new CreateVoteRequestDto("title", false, false,
          List.of("option1", "option2"), post.getId());
    }

    @Test
    @DisplayName("성공 - 투표 생성 테스트")
    public void createVote_ShouldSuccess() {
      // given
      given(postRepository.findById(createVoteRequestDto.getPostId())).willReturn(
          Optional.of(post));

      given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
      given(voteRepository.save(any(Vote.class))).willReturn(vote);
      given(voteOptionRepository.saveAll(anyList())).willAnswer(
          invocation -> invocation.getArgument(0));

      // when
      VoteResponseDto result = voteService.createVote(createVoteRequestDto, user);

      // then
      SoftAssertions.assertSoftly(softAssertions -> {
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getAllowAnonymous()).isEqualTo(false);
        assertThat(result.getAllowMultiple()).isEqualTo(false);

        assertThat(result.getOptions()).hasSize(2);
        assertThat(result.getOptions()).extracting(VoteOptionResponseDto::getVoteCount)
            .containsExactlyElementsOf(List.of(0, 0));
        assertThat(result.getOptions()).extracting(VoteOptionResponseDto::getOptionName)
            .containsExactlyElementsOf(List.of("option1", "option2"));

        assertThat(result.getIsOwner()).isEqualTo(true);
        assertThat(result.getHasVoted()).isEqualTo(false);
        assertThat(result.getIsEnd()).isEqualTo(false);

        assertThat(result.getTotalVoteCount()).isEqualTo(0);
        assertThat(result.getTotalUserCount()).isEqualTo(0);
      });
    }

    @Test
    @DisplayName("실패 - 게시물 없을 경우 투표 생성 실패")
    public void createVote_FailWhenPostNotExist() {
      // given
      given(postRepository.findById(createVoteRequestDto.getPostId())).willReturn(
          Optional.of(post));

      given(postRepository.findById(post.getId())).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> voteService.createVote(createVoteRequestDto, user))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("게시글을 찾을 수 없습니다.")
          .extracting("errorCode")
          .isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);
    }

    @Test
    @DisplayName("실패 - 게시물 생성자, 작성자 다른 경우")
    public void createVote_FailWhenWriterAndUserDifferent() {
      // given
      given(postRepository.findById(createVoteRequestDto.getPostId())).willReturn(
          Optional.of(post));

      given(postRepository.findById(post.getId())).willReturn(Optional.of(post));

      User anotherUser = ObjectFixtures.getUser();
      ReflectionTestUtils.setField(user, "id", "1");
      ReflectionTestUtils.setField(anotherUser, "id", "2");

      // when & then
      assertThatThrownBy(() -> voteService.createVote(createVoteRequestDto, anotherUser))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("투표 시작 권한이 존재하지 않습니다.")
          .extracting("errorCode")
          .isEqualTo(ErrorCode.API_NOT_ALLOWED);
    }
  }

  @Nested
  @DisplayName("투표 참여 테스트")
  class voteCastTest {

    CastVoteRequestDto castVoteRequestDto;
    VoteOption firstVoteOption;

    @BeforeEach
    public void setUp() {
      castVoteRequestDto = new CastVoteRequestDto();
      castVoteRequestDto.setVoteOptionIdList(List.of("id1"));
      firstVoteOption = voteOptions.get(0);
      firstVoteOption.updateVote(vote);
    }

    @Test
    @DisplayName("성공 - 투표 참여 테스트")
    public void castVote_ShouldSuccess() {
      // given
      given(voteOptionRepository.findById(any(String.class))).willReturn(
          Optional.of(firstVoteOption));
      given(voteRecordRepository.findByVoteOption_VoteAndUser(any(Vote.class),
          any(User.class))).willReturn(List.of());
      given(voteOptionRepository.findAllById(anyList())).willReturn(voteOptions.subList(0, 0));
      given(voteRecordRepository.saveAll(anyList())).willAnswer(
          invocation -> invocation.getArgument(0));

      // when
      String result = voteService.castVote(castVoteRequestDto, user);

      // then
      SoftAssertions.assertSoftly(softAssertions -> {
        assertThat(result).isEqualTo("투표 성공");
        verify(voteRecordRepository, times(1)).saveAll(anyList());
      });
    }

    @Test
    @DisplayName("실패 - voteOptionId 에 맞는 voteOption 이 없는 경우")
    public void castVote_FailWhenVoteOptionNotExist() {
      // given
      given(voteOptionRepository.findById(
          castVoteRequestDto.getVoteOptionIdList().get(0))).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> voteService.castVote(castVoteRequestDto, user)).isInstanceOf(
              BadRequestException.class).hasMessageContaining("존재하지 않는 투표 옵션입니다.")
          .extracting("errorCode")
          .isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);

      verify(voteRecordRepository, times(0)).saveAll(anyList());
    }

    @Test
    @DisplayName("실패 - 중복투표 비허용이지만 중복투표 한 경우")
    public void castVote_FailWhenSelectMultipleVoteOptionsNotAllowed() {
      // given
      given(voteOptionRepository.findById(any(String.class))).willReturn(
          Optional.of(firstVoteOption));
      castVoteRequestDto.setVoteOptionIdList(List.of("id1", "id2"));

      // when & then
      assertThatThrownBy(() -> voteService.castVote(castVoteRequestDto, user)).isInstanceOf(
              BadRequestException.class).hasMessageContaining("이 투표는 여러 항목을 선택할 수 없습니다.")
          .extracting("errorCode")
          .isEqualTo(ErrorCode.INVALID_PARAMETER);

      verify(voteRecordRepository, times(0)).saveAll(anyList());
    }

    @Test
    @DisplayName("실패 - 이미 투표 진행한 경우")
    public void castVote_FailWhenVoteRecordAlreadyExist() {
      // given
      given(voteOptionRepository.findById(any(String.class))).willReturn(
          Optional.of(firstVoteOption));
      given(voteRecordRepository.findByVoteOption_VoteAndUser(any(Vote.class),
          any(User.class))).willReturn(List.of(VoteRecord.of(user, firstVoteOption)));

      // when & then
      assertThatThrownBy(() -> voteService.castVote(castVoteRequestDto, user)).isInstanceOf(
              BadRequestException.class).hasMessageContaining("해당 투표에 이미 참여한 이력이 있습니다.")
          .extracting("errorCode")
          .isEqualTo(ErrorCode.INVALID_PARAMETER);

      verify(voteRecordRepository, times(0)).saveAll(anyList());
    }
  }

}
