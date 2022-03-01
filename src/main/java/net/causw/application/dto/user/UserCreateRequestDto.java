package net.causw.application.dto.user;

import lombok.Data;

@Data
public class UserCreateRequestDto {
    private String email;
    private String name;
    private String password;
    private String studentId;
    private Integer admissionYear;
    private String profileImage;
}
