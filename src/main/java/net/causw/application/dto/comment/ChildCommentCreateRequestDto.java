package net.causw.application.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;

@Data
@AllArgsConstructor
public class ChildCommentCreateRequestDto {
    private String content;
    private String parentCommentId;
    private String refChildComment;
    
    public Optional<String> getRefChildComment() {
        return Optional.ofNullable(this.refChildComment);
    }
}
