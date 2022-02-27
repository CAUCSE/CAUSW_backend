package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserFindEmailRequestDto {
    private String email;
    private String studentId;
}
