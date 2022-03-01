package net.causw.application.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class UserPrivilegedResponseDto {
    private List<UserResponseDto> councilUsers;
    private List<UserResponseDto> leaderGradeUsers;
    private List<UserResponseDto> leaderCircleUsers;
    private UserResponseDto leaderAlumni;

    private UserPrivilegedResponseDto(
            List<UserResponseDto> councilUsers,
            List<UserResponseDto> leaderGradeUsers,
            List<UserResponseDto> leaderCircleUsers,
            UserResponseDto leaderAlumni
    ) {
        this.councilUsers = councilUsers;
        this.leaderGradeUsers = leaderGradeUsers;
        this.leaderCircleUsers = leaderCircleUsers;
        this.leaderAlumni = leaderAlumni;
    }

    public static UserPrivilegedResponseDto from(
            List<UserResponseDto> councilUsers,
            List<UserResponseDto> leaderGrade1,
            List<UserResponseDto> leaderGrade2,
            List<UserResponseDto> leaderGrade3,
            List<UserResponseDto> leaderGrade4,
            List<UserResponseDto> leaderCircleUsers,
            List<UserResponseDto> leaderAlumniUsers
    ) {
        List<UserResponseDto> leaderGradeUsers = new LinkedList<>(leaderGrade1);
        leaderGradeUsers.addAll(leaderGrade2);
        leaderGradeUsers.addAll(leaderGrade3);
        leaderGradeUsers.addAll(leaderGrade4);

        return new UserPrivilegedResponseDto(
                councilUsers,
                leaderGradeUsers,
                leaderCircleUsers,
                leaderAlumniUsers
                        .stream()
                        .findFirst()
                        .orElse(null)
        );
    }
}
