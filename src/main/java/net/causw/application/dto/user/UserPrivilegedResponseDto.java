package net.causw.application.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class UserPrivilegedResponseDto {
    private UserResponseDto presidentUser;
    private UserResponseDto vicePresidentUser;
    private List<UserResponseDto> councilUsers;
    private List<UserResponseDto> leaderGradeUsers;
    private List<UserResponseDto> leaderCircleUsers;
    private UserResponseDto leaderAlumni;

    private UserPrivilegedResponseDto(
            UserResponseDto presidentUser,
            UserResponseDto vicePresidentUser,
            List<UserResponseDto> councilUsers,
            List<UserResponseDto> leaderGradeUsers,
            List<UserResponseDto> leaderCircleUsers,
            UserResponseDto leaderAlumniUser
    ) {
        this.presidentUser = presidentUser;
        this.vicePresidentUser = vicePresidentUser;
        this.councilUsers = councilUsers;
        this.leaderGradeUsers = leaderGradeUsers;
        this.leaderCircleUsers = leaderCircleUsers;
        this.leaderAlumni = leaderAlumniUser;
    }

    public static UserPrivilegedResponseDto from(
            UserResponseDto presidentUser,
            UserResponseDto vicePresidentUser,
            List<UserResponseDto> councilUsers,
            List<UserResponseDto> leaderGrade1,
            List<UserResponseDto> leaderGrade2,
            List<UserResponseDto> leaderGrade3,
            List<UserResponseDto> leaderGrade4,
            List<UserResponseDto> leaderCircleUsers,
            UserResponseDto leaderAlumniUser
    ) {
        List<UserResponseDto> leaderGradeUsers = new LinkedList<>(leaderGrade1);
        leaderGradeUsers.addAll(leaderGrade2);
        leaderGradeUsers.addAll(leaderGrade3);
        leaderGradeUsers.addAll(leaderGrade4);

        return new UserPrivilegedResponseDto(
                presidentUser,
                vicePresidentUser,
                councilUsers,
                leaderGradeUsers,
                leaderCircleUsers,
                leaderAlumniUser
        );
    }
}
