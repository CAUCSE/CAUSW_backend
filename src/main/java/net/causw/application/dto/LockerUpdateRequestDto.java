package net.causw.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LockerUpdateRequestDto {
    private String action;
    private String message;

    public Optional<String> getMessage() {
        return Optional.ofNullable(this.message);
    }
}
