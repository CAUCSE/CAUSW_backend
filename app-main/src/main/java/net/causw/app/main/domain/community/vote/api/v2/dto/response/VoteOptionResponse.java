package net.causw.app.main.domain.community.vote.api.v2.dto.response;

import java.util.List;

import net.causw.app.main.domain.community.vote.entity.VoteOption;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VoteOptionResponse {

	private String optionId;
	private String optionName;
	private int orderIndex;
	private int voteCount;
	private boolean votedByMe;
	private List<String> voteUsers;

	public static VoteOptionResponse of(
		VoteOption option,
		int orderIndex,
		int voteCount,
		boolean votedByMe,
		List<String> voteUsers) {
		return new VoteOptionResponse(
			option.getId(),
			option.getOptionName(),
			orderIndex,
			voteCount,
			votedByMe,
			voteUsers);
	}
}
