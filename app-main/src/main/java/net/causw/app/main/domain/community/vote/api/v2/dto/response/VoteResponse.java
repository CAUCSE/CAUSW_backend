package net.causw.app.main.domain.community.vote.api.v2.dto.response;

import java.util.List;

import net.causw.app.main.domain.community.vote.entity.Vote;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VoteResponse {

	private String voteId;
	private String postId;
	private String title;
	private boolean allowAnonymous;
	private boolean allowMultiple;
	private boolean isEnd;
	private boolean isOwner;
	private boolean hasVoted;
	private int totalVoteCount;
	private int totalUserCount;
	private List<VoteOptionResponse> options;

	public static VoteResponse of(
		Vote vote,
		boolean isOwner,
		boolean hasVoted,
		int totalVoteCount,
		int totalUserCount,
		List<VoteOptionResponse> options) {
		return new VoteResponse(
			vote.getId(),
			vote.getPost().getId(),
			vote.getTitle(),
			vote.isAllowAnonymous(),
			vote.isAllowMultiple(),
			vote.isEnd(),
			isOwner,
			hasVoted,
			totalVoteCount,
			totalUserCount,
			options);
	}
}
