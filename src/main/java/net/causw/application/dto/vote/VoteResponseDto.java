package net.causw.application.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class VoteResponseDto {
    private String voteId;
    private String title;
    private boolean allowAnonymous;
    private boolean allowMultiple;
    private List<VoteOptionResponseDto> options;
    private String postId;
    private boolean isOwner;
}
