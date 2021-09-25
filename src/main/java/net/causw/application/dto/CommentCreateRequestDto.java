package net.causw.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateRequestDto {
    private String content;
    private String postId;
    private String parentCommentId;

    public Optional<String> getParentCommentId() {
        return Optional.ofNullable(this.parentCommentId);
    }
}
