package net.causw.application.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateRequestDto {
    private String title;
    private String content;
    private String boardId;
    private List<String> attachmentList;

    public List<String> getAttachmentList() {
        return Optional.ofNullable(this.attachmentList).orElse(List.of());
    }
}
