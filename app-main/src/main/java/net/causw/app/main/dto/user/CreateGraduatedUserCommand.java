package net.causw.app.main.dto.user;

public record CreateGraduatedUserCommand(
        String email,
        String name,
        String studentId,
        Integer admissionYear,
        Integer graduationYear,
        String nickname,
        String major,
        String phoneNumber
) {}
