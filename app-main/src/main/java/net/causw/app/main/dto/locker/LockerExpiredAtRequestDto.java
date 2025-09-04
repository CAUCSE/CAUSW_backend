package net.causw.app.main.dto.locker;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
	@NotNull(message = "사용 만료일을 입력해 주세요.")
	@Schema(description = "Expiration date and time", example = "2024-09-01T11:41", requiredMode = Schema.RequiredMode.REQUIRED)
	private LocalDateTime expiredAt;
}
