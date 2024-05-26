package net.causw.application.dto.user;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequestDto {

    @ApiModelProperty(value = "이메일", example = "yebin@cau.ac.kr", required = true)
    private String email;

    @ApiModelProperty(value = "이름", example = "이예빈", required = true)
    private String name;

    @ApiModelProperty(value = "비밀번호", example = "password00!!", required = true)
    private String password;

    @ApiModelProperty(value = "학번", example = "20209999", required = true)
    private String studentId;

    @ApiModelProperty(value = "입학년도", example = "2020", required = true)
    private Integer admissionYear;

    @ApiModelProperty(value = "프로필 이미지 URL", example = "", required = true)
    private String profileImage;


    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(email)
                .name(name)
                .password(encodedPassword)
                .studentId(studentId)
                .admissionYear(admissionYear)
                .profileImage(profileImage)
                .build();
    }
}
