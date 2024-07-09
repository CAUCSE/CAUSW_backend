package net.causw.application.dto.duplicate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DuplicatedCheckResponseDto {

    @Schema(description = "중복 여부 boolean 값", example = "true")
    private Boolean result;

    private DuplicatedCheckResponseDto(boolean result) {
        this.result = result;
    }

    public static DuplicatedCheckResponseDto of(boolean result) {
        return new DuplicatedCheckResponseDto(result);
    }
}
