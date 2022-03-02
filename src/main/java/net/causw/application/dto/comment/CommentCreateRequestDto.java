package net.causw.application.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentCreateRequestDto {
    private String content;
    private String postId;
}
