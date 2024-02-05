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
public class CircleCreateRequestDto {

    @ApiModelProperty(value = "동아리 이름", example = "소프트웨어학부 특별기구 ICT위원회 동문네트워크")
    private String name;

    @ApiModelProperty(value = "동아리 메인 이미지, 없애기 가능(nullable)", example = "string")
    private String mainImage;

    @ApiModelProperty(value = "동아리 설명", example = "ICT위원회는 동문 네트워크 서비스를 만드는 특별기구이자 동아리입니다.")
    private String description;

    @ApiModelProperty(value = "동아리장 ID", example = "UUID 형식의 동아리장 ID(PK) String 값입니다.")
    private String leaderId;
}
