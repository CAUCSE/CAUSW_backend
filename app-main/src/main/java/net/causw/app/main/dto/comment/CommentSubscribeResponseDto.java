package net.causw.app.main.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentSubscribeResponseDto {
    @Schema(description = "댓글 id 값", example = "uuid 형식의 String 값입니다.")
    private String commentId;

    @Schema(description = "유저 id값", example = "uuid 형식의 String 값입니다.")
    private String userId;

    @Schema(description = "구독 여부", example = "true")
    private Boolean isSubscribed;
}
