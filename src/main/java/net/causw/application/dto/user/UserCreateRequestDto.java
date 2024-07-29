package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @Schema(description = "이메일", example = "yebin@cau.ac.kr")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    @NotBlank(message = "이메일을 입력해 주세요.")
    private String email;

    @Schema(description = "이름", example = "이예빈")
    @NotBlank(message = "이름을 입력해 주세요.")
    private String name;

    @Schema(description = "비밀번호", example = "password00!!")
    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private String password;

    @Schema(description = "학번", example = "20209999")
    private String studentId;

    @Schema(description = "입학년도", example = "2020")
    @NotBlank(message = "입학 년도를 입력해 주세요.")
    private Integer admissionYear;

    @Schema(description = "프로필 이미지 URL", example = "")
    private String profileImage;
<<<<<<< HEAD
    public User toEntity(String encodedPassword, Set<Role> roles, UserState state) {
=======

    public User toEntity(String encodedPassword, Role role, UserState state) {
>>>>>>> fd3bc17 (refactor: User 관련 dto에 Valid 적용)
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
