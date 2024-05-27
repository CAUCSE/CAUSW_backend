package net.causw.application.dto.user;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDto {

    @ApiModelProperty(value = "이메일", example = "yebin@cau.ac.kr")
    private String email;

    @ApiModelProperty(value = "이름", example = "이에빈")
    private String name;

    @ApiModelProperty(value = "학번", example = "20209999")
    private String studentId;

    @ApiModelProperty(value = "입학년도", example = "2020")
    private Integer admissionYear;

    @ApiModelProperty(value = "프로필 이미지 URL", example = "")
    private String profileImage;
}
