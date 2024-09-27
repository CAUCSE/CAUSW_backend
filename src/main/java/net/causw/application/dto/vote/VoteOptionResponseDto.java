package net.causw.application.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.causw.application.dto.user.UserResponseDto;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class VoteOptionResponseDto {
    private String id;
    private String optionName;
    private Long voteCount;
    private List<UserResponseDto> voteUsers;
}
