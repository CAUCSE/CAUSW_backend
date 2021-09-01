package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DuplicatedCheckDto {
    private Boolean result;

    private DuplicatedCheckDto(boolean result) {
        this.result = result;
    }

    public static DuplicatedCheckDto of(boolean result) {
        return new DuplicatedCheckDto(result);
    }
}
