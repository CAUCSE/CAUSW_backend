package net.causw.application.dto.inquiry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InquiryCreateRequestDto {
    private String title;
    private String content;
}
