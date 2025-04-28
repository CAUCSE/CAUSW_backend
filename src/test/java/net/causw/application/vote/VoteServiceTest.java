package net.causw.application.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
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
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.util.StatusUtil;
import net.causw.application.dto.util.dtoMapper.UserDtoMapper;
import net.causw.application.dto.util.dtoMapper.VoteDtoMapper;
import net.causw.application.dto.vote.CreateVoteRequestDto;
import net.causw.application.dto.vote.VoteOptionResponseDto;
import net.causw.application.dto.vote.VoteResponseDto;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.util.ObjectFixtures;
import net.causw.domain.model.util.StaticValue;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
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
    @DisplayName("투표 생성 성공 테스트")
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
            .containsExactlyElementsOf(List.of(0,0));
        assertThat(result.getOptions()).extracting(VoteOptionResponseDto::getOptionName)
            .containsExactlyElementsOf(List.of("option1", "option2"));

        assertThat(result.getIsOwner()).isEqualTo(true);
        assertThat(result.getHasVoted()).isEqualTo(false);
        assertThat(result.getIsEnd()).isEqualTo(false);

        assertThat(result.getTotalVoteCount()).isEqualTo(0);
        assertThat(result.getTotalUserCount()).isEqualTo(0);
      });
    }
  }
}
