package net.causw.app.main.service.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.app.main.repository.vote.VoteOptionRepository;
import net.causw.app.main.repository.vote.VoteRecordRepository;
import net.causw.app.main.repository.vote.VoteRepository;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.vote.Vote;
import net.causw.app.main.domain.model.entity.vote.VoteOption;
import net.causw.app.main.domain.model.entity.vote.VoteRecord;
import net.causw.app.main.dto.vote.CastVoteRequestDto;
import net.causw.app.main.dto.vote.CreateVoteRequestDto;
import net.causw.app.main.dto.vote.VoteOptionResponseDto;
import net.causw.app.main.dto.vote.VoteResponseDto;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.app.main.util.ObjectFixtures;

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

	User writerUser;
	Post post;
	Board board;
	List<VoteOption> voteOptions;
	Vote vote;

	@BeforeEach
	public void setUp() {
		writerUser = ObjectFixtures.getUser();
		board = ObjectFixtures.getBoard();
		post = ObjectFixtures.getPost(writerUser, board);
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
			VoteResponseDto result = voteService.createVote(createVoteRequestDto, writerUser);

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
			assertThatThrownBy(() -> voteService.createVote(createVoteRequestDto, writerUser))
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
			ReflectionTestUtils.setField(writerUser, "id", "1");
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
			String result = voteService.castVote(castVoteRequestDto, writerUser);

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
			assertThatThrownBy(() -> voteService.castVote(castVoteRequestDto, writerUser)).isInstanceOf(
					BadRequestException.class).hasMessageContaining("존재하지 않는 투표 옵션입니다.")
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);

			verify(voteRecordRepository, never()).saveAll(anyList());
		}

		@Test
		@DisplayName("실패 - 중복투표 비허용이지만 중복투표 한 경우")
		public void castVote_FailWhenSelectMultipleVoteOptionsNotAllowed() {
			// given
			given(voteOptionRepository.findById(any(String.class))).willReturn(
				Optional.of(firstVoteOption));
			castVoteRequestDto.setVoteOptionIdList(List.of("id1", "id2"));

			// when & then
			assertThatThrownBy(() -> voteService.castVote(castVoteRequestDto, writerUser)).isInstanceOf(
					BadRequestException.class).hasMessageContaining("이 투표는 여러 항목을 선택할 수 없습니다.")
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_PARAMETER);

			verify(voteRecordRepository, never()).saveAll(anyList());
		}

		@Test
		@DisplayName("실패 - 이미 투표 진행한 경우")
		public void castVote_FailWhenVoteRecordAlreadyExist() {
			// given
			given(voteOptionRepository.findById(any(String.class))).willReturn(
				Optional.of(firstVoteOption));
			given(voteRecordRepository.findByVoteOption_VoteAndUser(any(Vote.class),
				any(User.class))).willReturn(List.of(VoteRecord.of(writerUser, firstVoteOption)));

			// when & then
			assertThatThrownBy(() -> voteService.castVote(castVoteRequestDto, writerUser)).isInstanceOf(
					BadRequestException.class).hasMessageContaining("해당 투표에 이미 참여한 이력이 있습니다.")
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_PARAMETER);

			verify(voteRecordRepository, never()).saveAll(anyList());
		}
	}

	@Nested
	@DisplayName("투표 종료 테스트")
	class entVoteTest {

		private String voteId;

		@BeforeEach
		public void setUp() {
			voteId = "id1";
		}

		@Test
		@DisplayName("성공 - 투표 종료 테스트")
		public void endVote_ShouldSuccess() {
			// given
			given(voteRepository.findById(voteId)).willReturn(Optional.of(vote));

			// when
			VoteResponseDto result = voteService.endVote(voteId, writerUser);

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
				assertThat(result.getIsEnd()).isEqualTo(true);

				assertThat(result.getTotalVoteCount()).isEqualTo(0);
				assertThat(result.getTotalUserCount()).isEqualTo(0);
			});
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 투표일때")
		public void endVote_FailWhenVoteNotExist() {
			// given
			given(voteRepository.findById(voteId)).willReturn(Optional.empty());

			// when
			assertThatThrownBy(() -> voteService.endVote(voteId, writerUser))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("투표가 존재하지 않습니다.")
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);

			verify(voteRepository, never()).save(any());
		}

		@Test
		@DisplayName("실패 - 투표 종료자가 게시글 작성자가 아닌 경우")
		public void endVote_FailWhenUserIsNotWriter() {
			// given
			given(voteRepository.findById(voteId)).willReturn(Optional.of(vote));

			User anotherUser = ObjectFixtures.getUser();
			ReflectionTestUtils.setField(writerUser, "id", "1");
			ReflectionTestUtils.setField(anotherUser, "id", "2");

			// when
			assertThatThrownBy(() -> voteService.endVote(voteId, anotherUser))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("투표 종료 권한이 존재하지 않습니다.")
				.extracting("errorCode")
				.isEqualTo(ErrorCode.API_NOT_ALLOWED);

			verify(voteRepository, never()).save(any());
		}

		@Test
		@DisplayName("실패 - 이미 종료된 투표인 경우")
		public void endVote_FailWhenVoteAlreadyEnded() {
			// given
			Vote mockVote = mock(Vote.class);
			given(voteRepository.findById(voteId)).willReturn(Optional.of(mockVote));
			given(mockVote.getPost()).willReturn(post);
			given(mockVote.isEnd()).willReturn(true);

			// when
			assertThatThrownBy(() -> voteService.endVote(voteId, writerUser))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("이미 종료된 투표입니다.")
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_PARAMETER);

			verify(voteRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("투표 재시작 테스트")
	class restartVoteTest {

		private String voteId;
		private Vote mockVote;

		@BeforeEach
		public void setUp() {
			voteId = "id1";
			mockVote = spy(vote);
		}

		@Test
		@DisplayName("성공 - 투표 재시작 테스트")
		public void restartVote_ShouldSuccess() {
			// given
			given(mockVote.getPost()).willReturn(post);
			given(mockVote.isEnd()).willReturn(true);
			given(voteRepository.findById(voteId)).willReturn(Optional.of(mockVote));

			// restartVote 호출될 때 isEnd를 false로 바꾸는 행동 추가
			doAnswer(invocation -> {
				given(mockVote.isEnd()).willReturn(false);
				return null;
			}).when(mockVote).restartVote();

			// when
			VoteResponseDto result = voteService.restartVote(voteId, writerUser);

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
		@DisplayName("실패 - 존재하지 않는 투표일때")
		public void endVote_FailWhenVoteNotExist() {
			// given
			given(voteRepository.findById(voteId)).willReturn(Optional.empty());

			// when
			assertThatThrownBy(() -> voteService.restartVote(voteId, writerUser))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("투표가 존재하지 않습니다.")
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);

			verify(voteRepository, never()).save(any());
		}

		@Test
		@DisplayName("실패 - 투표 종료자가 게시글 작성자가 아닌 경우")
		public void castVote_FailWhenUserIsNotWriter() {
			// given
			given(mockVote.getPost()).willReturn(post);
			given(voteRepository.findById(voteId)).willReturn(Optional.of(mockVote));

			User anotherUser = ObjectFixtures.getUser();
			ReflectionTestUtils.setField(writerUser, "id", "1");
			ReflectionTestUtils.setField(anotherUser, "id", "2");

			// when
			assertThatThrownBy(() -> voteService.restartVote(voteId, anotherUser))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("투표 재시작 권한이 존재하지 않습니다.")
				.extracting("errorCode")
				.isEqualTo(ErrorCode.API_NOT_ALLOWED);

			verify(voteRepository, never()).save(any());
		}

		@Test
		@DisplayName("실패 - 이미 종료된 투표인 경우")
		public void endVote_FailWhenVoteAlreadyEnded() {
			// given
			given(mockVote.getPost()).willReturn(post);
			given(voteRepository.findById(voteId)).willReturn(Optional.of(mockVote));
			given(mockVote.isEnd()).willReturn(false);

			// when
			assertThatThrownBy(() -> voteService.restartVote(voteId, writerUser))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("종료되지 않은 투표입니다.")
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_PARAMETER);

			verify(voteRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("투표 Id로 검색 테스트")
	class getVoteByIdTest {

		private String voteId;

		@BeforeEach
		public void setUp() {
			voteId = "id1";
		}

		@Test
		@DisplayName("성공 - 투표 ID로 투표 조회")
		void getVoteById_ShouldSuccess() {
			// given
			given(voteRepository.findById(voteId)).willReturn(Optional.of(vote));

			// when
			VoteResponseDto result = voteService.getVoteById(voteId, writerUser);

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
		@DisplayName("실패 - 투표가 존재하지 않는 경우")
		void getVoteById_ShouldThrowException_WhenVoteNotFound() {
			// given
			given(voteRepository.findById(voteId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> voteService.getVoteById(voteId, writerUser))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("투표가 존재하지 않습니다.")
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);
		}
	}

}
