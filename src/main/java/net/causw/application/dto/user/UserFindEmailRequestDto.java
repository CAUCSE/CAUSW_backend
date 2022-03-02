package net.causw.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserFindEmailRequestDto {
    private String email;
    private String studentId;
}
