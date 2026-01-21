package net.causw.app.main.domain.community.vote.api.v1.dto;

import java.util.List;

import net.causw.app.main.domain.user.account.api.v1.dto.UserResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VoteOptionResponseDto {
	private String id;
	private String optionName;
	private Integer voteCount;
	private List<UserResponseDto> voteUsers;
}
