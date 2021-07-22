package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserCreateRequestDto {
    private String email;
    private String name;
    private String password;
    private String studentId;
    private Integer admissionYear;
    // TODO: profile image
}
