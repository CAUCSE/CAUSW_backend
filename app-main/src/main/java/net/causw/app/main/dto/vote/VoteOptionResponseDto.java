package net.causw.app.main.dto.vote;

import java.util.List;

import net.causw.app.main.dto.user.UserResponseDto;

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
