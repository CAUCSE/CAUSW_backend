package net.causw.app.main.dto.inquiry;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InquiryCreateRequestDto {
    @NotBlank(message = "설문 제목을 입력해 주세요.")
    private String title;
    @NotBlank(message = "설문 내용을 입력해 주세요.")
    private String content;
}
