package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSignInRequestDto {

    @Schema(description = "이메일", example = "yebin@cau.ac.kr")
    private String email;

    @Schema(description = "비밀번호", example = "password00!!")
    private String password;
}
