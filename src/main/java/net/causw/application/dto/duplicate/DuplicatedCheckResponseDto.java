package net.causw.application.dto.duplicate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DuplicatedCheckResponseDto {
    private Boolean result;

    private DuplicatedCheckResponseDto(boolean result) {
        this.result = result;
    }

    public static DuplicatedCheckResponseDto of(boolean result) {
        return new DuplicatedCheckResponseDto(result);
    }
}
