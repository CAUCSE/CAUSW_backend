package net.causw.app.main.dto.form.response.reply;

import java.util.List;

import net.causw.app.main.dto.form.response.QuestionResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReplyResponseDto {

	@Schema(description = "질문 Dto List")
	private List<QuestionResponseDto> questionResponseDtoList;

	@Schema(description = "해당 사용자의 질문별 답변 리스트")
	private List<ReplyQuestionResponseDto> replyQuestionResponseDtoList;
}
