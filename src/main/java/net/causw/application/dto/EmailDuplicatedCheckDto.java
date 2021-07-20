package net.causw.application.dto;

import lombok.Getter;

@Getter
public class EmailDuplicatedCheckDto {
    private Boolean result;

    private EmailDuplicatedCheckDto(boolean result) {
        this.result = result;
    }

    public static EmailDuplicatedCheckDto of(boolean result) {
        return new EmailDuplicatedCheckDto(result);
    }
}
