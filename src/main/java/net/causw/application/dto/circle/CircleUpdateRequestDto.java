package net.causw.application.dto.circle;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CircleUpdateRequestDto {

    @ApiModelProperty(value = "동아리 이름", example = "소프트웨어학부 특별기구 ICT위원회 동문 네트워크")
    private String name;

    @ApiModelProperty(value = "동아리 메인 이미지(nullable)", example = "String")
    private String mainImage;

    @ApiModelProperty(value = "동아리 설명", example = "ICT위원회는 동문 네트워크 서비스를 만드는 특별기구이자 동아리입니다.")
    private String description;
}
