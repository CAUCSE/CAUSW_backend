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
public class ChildCommentCreateRequestDto {
    private String content;
    private String parentCommentId;
    private String tagUserName;
    private String refChildComment;

    public Optional<String> getTagUserName() {
        return Optional.ofNullable(this.tagUserName);
    }
    
    public Optional<String> getRefChildComment() {
        return Optional.ofNullable(this.refChildComment);
    }
}
