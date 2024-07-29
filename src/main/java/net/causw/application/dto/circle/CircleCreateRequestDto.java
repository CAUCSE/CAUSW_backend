package net.causw.application.dto.circle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CircleCreateRequestDto {

    @Schema(description = "동아리 이름", example = "소프트웨어학부 특별기구 ICT위원회 동문네트워크")
<<<<<<< HEAD
    @NotBlank(message = "동아리 이름은 필수 입력값입니다.")
=======
    @NotBlank(message = "동아리 이름을 입력해 주세요.")
>>>>>>> 8b8cf77 (refactor: @NotBlank로 변경)
    private String name;

    @Schema(description = "동아리 메인 이미지, 없애기 가능(nullable)", example = "string")
    private String mainImage;

    @Schema(description = "동아리 설명", example = "ICT위원회는 동문 네트워크 서비스를 만드는 특별기구이자 동아리입니다.")
    private String description;

    @Schema(description = "동아리장 ID", example = "UUID 형식의 동아리장 ID(PK) String 값입니다.")
    private String leaderId;
}
