package net.causw.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserUpdateRequestDto {
    private String email;
    private String name;
    private String studentId;
    private Integer admissionYear;
    private String profileImage;
}
