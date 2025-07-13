package net.causw.app.main.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.causw.app.main.dto.user.UserResponseDto;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class VoteOptionResponseDto {
    private String id;
    private String optionName;
    private Integer voteCount;
    private List<UserResponseDto> voteUsers;
}
