package net.causw.application.dto.circle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CircleCreateRequestDto {

    @NotBlank(message = "동아리 이름을 입력해 주세요.")
    @Schema(description = "동아리 이름", example = "소프트웨어학부 특별기구 ICT위원회 동문네트워크")
    private String name;

    @Schema(description = "동아리 설명", example = "ICT위원회는 동문 네트워크 서비스를 만드는 특별기구이자 동아리입니다.")
    private String description;

    @Schema(description = "동아리장 ID", example = "UUID 형식의 동아리장 ID(PK) String 값입니다.")
    @Pattern(
            regexp = "^[0-9a-fA-F]{32}$",
            message = "동아리장 id 값은 대시(-) 없이 32자리의 UUID 형식이어야 합니다."
    )
    private String leaderId;

    @Schema(description = "동아리 회비", example = "5000")
    @Positive(message = "동아리 회비는 0보다 커야 합니다.")
    private Integer circleTax;

    @Schema(description = "동아리 모집인원", example = "10")
    @Positive(message = "동아리 모집인원은 0보다 커야 합니다.")
    private Integer recruitMembers;
}
