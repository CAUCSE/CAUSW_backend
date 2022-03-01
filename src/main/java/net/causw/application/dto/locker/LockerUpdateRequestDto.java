package net.causw.application.dto.locker;

import lombok.Data;

import java.util.Optional;

@Data
public class LockerUpdateRequestDto {
    private String action;
    private String message;

    public Optional<String> getMessage() {
        return Optional.ofNullable(this.message);
    }
}
