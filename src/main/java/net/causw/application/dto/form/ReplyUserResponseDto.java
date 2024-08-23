package net.causw.application.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyUserResponseDto {
    private String userId;
    private String userName; // 추가적으로 필요한 사용자 정보들
    private List<QuestionReplyResponseDto> replies; // 해당 사용자의 질문별 답변 리스트
}
