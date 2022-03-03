package net.causw.application.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChildCommentCreateRequestDto {
    private String content;
    private String parentCommentId;
    private String refChildComment;

    public Optional<String> getRefChildComment() {
        return Optional.ofNullable(this.refChildComment);
    }
}
