package net.causw.application.dto.user;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequestDto {

    @Schema(description = "이메일", example = "yebin@cau.ac.kr", required = true)
    private String email;

    @Schema(description = "이름", example = "이예빈", required = true)
    private String name;

    @Schema(description = "비밀번호", example = "password00!!", required = true)
    private String password;

    @Schema(description = "학번", example = "20209999", required = true)
    private String studentId;

    @Schema(description = "입학년도", example = "2020", required = true)
    private Integer admissionYear;

    @Schema(description = "프로필 이미지 URL", example = "", required = true)
    private String profileImage;
    public User toEntity(String encodedPassword, Set<Role> roles, UserState state) {
        return User.builder()
                .email(email)
                .name(name)
                .roles(roles)
                .state(state)
                .password(encodedPassword)
                .studentId(studentId)
                .admissionYear(admissionYear)
                .profileImage(profileImage)
                .build();
    }
}
