package net.causw.app.main.domain.community.vote.api.v1.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VoteResponseDto {
	private String voteId;
	private String title;
	private Boolean allowAnonymous;
	private Boolean allowMultiple;
	private List<VoteOptionResponseDto> options;
	private String postId;
	private Boolean isOwner;
	private Boolean hasVoted;
	private Boolean isEnd;
	private Integer totalVoteCount;
	private Integer totalUserCount;
}
