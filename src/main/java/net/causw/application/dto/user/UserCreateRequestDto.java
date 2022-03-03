package net.causw.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequestDto {
    private String email;
    private String name;
    private String password;
    private String studentId;
    private Integer admissionYear;
    private String profileImage;
}
