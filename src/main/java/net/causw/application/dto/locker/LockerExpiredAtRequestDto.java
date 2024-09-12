package net.causw.application.dto.locker;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LockerExpiredAtRequestDto {
    @NotBlank(message = "사용 만료일을 입력해 주세요.")
    @Schema(description = "Expiration date and time", example = "2024-09-01T11:41", required = true)
    private LocalDateTime expiredAt;
}
