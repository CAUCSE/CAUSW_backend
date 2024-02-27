package net.causw.application.dto.locker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LockerUpdateRequestDto {
    private String action;
    private String message;

    public Optional<String> getMessage() {
        return Optional.ofNullable(this.message);
    }
}
