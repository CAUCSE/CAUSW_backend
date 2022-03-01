package net.causw.application.dto.user;

import lombok.Data;

@Data
public class UserUpdateRequestDto {
    private String email;
    private String name;
    private String studentId;
    private Integer admissionYear;
    private String profileImage;
}
