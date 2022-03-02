package net.causw.application.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostUpdateRequestDto {
    private String title;
    private String content;
}
