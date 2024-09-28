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
    private Boolean allowAnonymous;
    private Boolean allowMultiple;
    private List<VoteOptionResponseDto> options;
    private String postId;
    private Boolean isOwner;
    private Boolean hasVoted;
    private Boolean isEnd;
}
