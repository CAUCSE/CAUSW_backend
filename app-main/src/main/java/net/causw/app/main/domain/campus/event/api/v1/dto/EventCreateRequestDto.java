package net.causw.app.main.domain.campus.event.api.v1.dto;

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
public class EventCreateRequestDto {

	@NotBlank(message = "이벤트 배너 URL은 필수 입력값 입니다.")
	@Schema(description = "이벤트 URL")
	private String url;

}
