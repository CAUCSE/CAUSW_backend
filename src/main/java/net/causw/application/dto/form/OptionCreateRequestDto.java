package net.causw.application.dto.form;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionCreateRequestDto {
    private Integer optionNumber;
    private String optionText;
    private Boolean isSelected;
}
