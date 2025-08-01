package net.causw.app.main.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EventUpdateRequestDto {

	@NotBlank(message = "이벤트 배너 URL은 필수 입력값 입니다.")
	@Schema(description = "이벤트 URL")
	private String url;

}
