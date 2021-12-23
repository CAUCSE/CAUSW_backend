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
    private Optional<String> tagUserName;
    private Optional<String> refChildComment;
}
