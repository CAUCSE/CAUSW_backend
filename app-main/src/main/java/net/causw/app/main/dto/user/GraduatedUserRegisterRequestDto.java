package net.causw.app.main.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GraduatedUserRegisterRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String password;

    private String studentId;

    @NotNull
    private Integer admissionYear;

    @NotNull
    private Integer graduationYear;

    @NotBlank
    private String nickname;

    @NotBlank
    private String major;

    @NotBlank
    @Pattern(regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식에 맞지 않습니다.")
    private String phoneNumber;
}
