package net.causw.application.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostUpdateRequestDto {
    private String title;
    private String content;
    private List<String> attachmentList;
}
