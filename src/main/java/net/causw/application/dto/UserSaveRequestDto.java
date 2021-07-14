package net.causw.application.dto;

import net.causw.infra.Role;
import net.causw.infra.User;

public class UserSaveRequestDto {
    private String email;
    private String name;
    private String password;
    private Integer admissionYear;

    private UserSaveRequestDto(
            String email,
            String name,
            String password,
            Integer admissionYear
    ) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.admissionYear = admissionYear;
    }

    public User toEntity() {
        return User.of(email, name, password, admissionYear, Role.VISITOR, null, false, null);
    }
}