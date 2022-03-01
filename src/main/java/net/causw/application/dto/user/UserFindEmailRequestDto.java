package net.causw.application.dto.user;

import lombok.Data;

@Data
public class UserFindEmailRequestDto {
    private String email;
    private String studentId;
}
