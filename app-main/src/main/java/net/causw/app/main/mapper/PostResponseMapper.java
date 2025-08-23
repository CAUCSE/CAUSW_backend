package net.causw.app.main.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.model.entity.form.Form;
import net.causw.app.main.domain.model.entity.form.FormQuestion;
import net.causw.app.main.domain.model.entity.form.FormQuestionOption;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.vote.Vote;
import net.causw.app.main.domain.model.entity.vote.VoteOption;
import net.causw.app.main.domain.model.entity.vote.VoteRecord;
import net.causw.app.main.domain.policy.StatusPolicy;
import net.causw.app.main.dto.comment.CommentResponseDto;
import net.causw.app.main.dto.form.response.FormResponseDto;
import net.causw.app.main.dto.form.response.OptionResponseDto;
import net.causw.app.main.dto.form.response.QuestionResponseDto;
import net.causw.app.main.dto.post.PostResponseDto;
import net.causw.app.main.dto.user.UserResponseDto;
import net.causw.app.main.dto.util.dtoMapper.FormDtoMapper;
import net.causw.app.main.dto.util.dtoMapper.PostDtoMapper;
import net.causw.app.main.dto.util.dtoMapper.UserDtoMapper;
import net.causw.app.main.dto.util.dtoMapper.VoteDtoMapper;
import net.causw.app.main.dto.vote.VoteOptionResponseDto;
import net.causw.app.main.dto.vote.VoteResponseDto;
import net.causw.app.main.repository.vote.VoteRecordRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostResponseMapper {

	private final PostDtoMapper postDtoMapper;
	// TODO: voteRecordRepository 관련 로직 mapper에서 분리
	private final VoteRecordRepository voteRecordRepository;

	public PostResponseDto toPostResponseDto(
		Post post,
		User user,
		Page<CommentResponseDto> commentsPage,
		Long numOfComments,
		Long numOfPostLikes,
		Long numOfPostFavorites,
		boolean postLiked,
		boolean postAlreadyFavorite,
		boolean isPostHasComment,
		Boolean postSubscribed) {
		PostResponseDto postResponseDto = postDtoMapper.toPostResponseDtoExtended(
			post,
			commentsPage,
			numOfComments,
			numOfPostLikes,
			numOfPostFavorites,
			postLiked,
			postAlreadyFavorite,
			StatusPolicy.isPostOwner(post, user),
			StatusPolicy.isUpdatable(post, user, isPostHasComment),
			StatusPolicy.isDeletable(post, user, post.getBoard(), isPostHasComment),
			StatusPolicy.isPostForm(post) ? toFormResponseDto(post.getForm()) : null,
			StatusPolicy.isPostVote(post) ? toVoteResponseDto(post.getVote(), user) : null,
			StatusPolicy.isPostVote(post),
			StatusPolicy.isPostForm(post),
			postSubscribed
		);

		// 화면에 표시할 작성자 닉네임 설정
		User writer = post.getWriter();
		postResponseDto.setDisplayWriterNickname(writer.getDisplayWriterNicName(post.getIsAnonymous()));

		return postResponseDto;
	}

	private FormResponseDto toFormResponseDto(Form form) {
		return FormDtoMapper.INSTANCE.toFormResponseDto(
			form,
			form.getFormQuestionList().stream()
				.map(this::toQuestionResponseDto)
				.collect(Collectors.toList())
		);
	}

	private QuestionResponseDto toQuestionResponseDto(FormQuestion formQuestion) {
		return FormDtoMapper.INSTANCE.toQuestionResponseDto(
			formQuestion,
			formQuestion.getFormQuestionOptionList().stream()
				.map(this::toOptionResponseDto)
				.collect(Collectors.toList())
		);
	}

	private OptionResponseDto toOptionResponseDto(FormQuestionOption formQuestionOption) {
		return FormDtoMapper.INSTANCE.toOptionResponseDto(formQuestionOption);
	}

	private VoteResponseDto toVoteResponseDto(Vote vote, User user) {
		List<VoteOptionResponseDto> voteOptionResponseDtoList = vote.getVoteOptions().stream()
			.sorted(Comparator.comparing(VoteOption::getCreatedAt))
			.map(this::tovoteOptionResponseDto)
			.collect(Collectors.toList());

		Set<String> uniqueUserIds = voteOptionResponseDtoList.stream()
			.flatMap(voteOptionResponseDto -> voteOptionResponseDto.getVoteUsers().stream())
			.map(UserResponseDto::getId)
			.collect(Collectors.toSet());
		Integer totalUserCount = uniqueUserIds.size();

		return VoteDtoMapper.INSTANCE.toVoteResponseDto(
			vote,
			voteOptionResponseDtoList
			, StatusPolicy.isVoteOwner(vote, user)
			, vote.isEnd()
			, voteRecordRepository.existsByVoteOption_VoteAndUser(vote, user)
			, voteOptionResponseDtoList.stream()
				.mapToInt(VoteOptionResponseDto::getVoteCount)
				.sum()
			, totalUserCount);
	}

	private VoteOptionResponseDto tovoteOptionResponseDto(VoteOption voteOption) {
		List<VoteRecord> voteRecords = voteRecordRepository.findAllByVoteOption(voteOption);
		List<UserResponseDto> userResponseDtos = voteRecords.stream()
			.map(voteRecord -> UserDtoMapper.INSTANCE.toUserResponseDto(voteRecord.getUser(), null, null))
			.collect(Collectors.toList());
		return VoteDtoMapper.INSTANCE.toVoteOptionResponseDto(voteOption, voteRecords.size(), userResponseDtos);
	}
}
