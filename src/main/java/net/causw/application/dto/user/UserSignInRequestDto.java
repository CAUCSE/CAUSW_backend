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
public class UserSignInRequestDto {

    @ApiModelProperty(value = "이메일", example = "yebin@cau.ac.kr")
    private String email;

    @ApiModelProperty(value = "비밀번호", example = "password00!!")
    private String password;
}
