package net.causw.application.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostUpdateRequestDto {

    @NotBlank(message = "게시글 제목을 입력해 주세요.")
    @Schema(description = "게시글 제목", example = "게시글의 제목입니다.")
    private String title;

    @NotBlank(message = "게시글 내용을 입력해 주세요.")
    @Schema(description = "게시글 내용", example = "게시글의 내용입니다.")
    private String content;

}
