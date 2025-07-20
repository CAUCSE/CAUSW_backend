package net.causw.app.main.dto.duplicate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DuplicatedCheckResponseDto {

    @Schema(description = "중복 여부 boolean 값", example = "true")
    private Boolean result;

}
