package net.causw.application.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EventCreateRequestDto {

    @NotBlank(message = "이벤트 배너 URL은 필수 입력값 입니다.")
    @Schema(description = "이벤트 URL")
    private String url;

    @NotNull(message = "이벤트 배너 이미지는 필수 입력값 입니다.")
    @Schema(description = "이미지")
    private MultipartFile image;
}
