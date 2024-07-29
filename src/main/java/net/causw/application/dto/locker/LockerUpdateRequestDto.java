package net.causw.application.dto.locker;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LockerUpdateRequestDto {

    @Schema(description = "Action to perform on the locker", allowableValues = {"ENABLE", "DISABLE", "REGISTER", "RETURN", "EXTEND"}, example = "REGISTER")
    @NotBlank(message = "활성화 여부를 선택해 주세요.")
    private String action;

    @Schema(description = "Message to be logged", example = "hi(자율)")
    private String message;

    public Optional<String> getMessage() {
        return Optional.ofNullable(this.message);
    }
}
