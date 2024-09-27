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
    private Boolean isEnd;

    // 빈 객체 생성 메서드
    public static VoteResponseDto empty() {
        return VoteResponseDto.builder()
                .voteId("")           // 기본값 null
                .title("")              // 빈 문자열
                .allowAnonymous(false)  // 기본값 false
                .allowMultiple(false)   // 기본값 false
                .options(List.of())     // 빈 리스트
                .postId("")           // 기본값 null
                .isOwner(false)         // 기본값 false
                .build();
    }
}
