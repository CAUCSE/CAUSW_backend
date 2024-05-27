package net.causw.application.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Builder
public class UserPrivilegedResponseDto {
    private List<UserResponseDto> presidentUser;
    private List<UserResponseDto> vicePresidentUser;
    private List<UserResponseDto> councilUsers;
    private List<UserResponseDto> leaderGradeUsers;
    private List<UserResponseDto> leaderCircleUsers;
    private List<UserResponseDto> leaderAlumni;

    public static UserPrivilegedResponseDto of(
            List<UserResponseDto> presidentUser,
            List<UserResponseDto> vicePresidentUser,
            List<UserResponseDto> councilUsers,
            List<UserResponseDto> leaderGrade1,
            List<UserResponseDto> leaderGrade2,
            List<UserResponseDto> leaderGrade3,
            List<UserResponseDto> leaderGrade4,
            List<UserResponseDto> leaderCircleUsers,
            List<UserResponseDto> leaderAlumniUser
    ) {
        List<UserResponseDto> leaderGradeUsers = new LinkedList<>(leaderGrade1);
        leaderGradeUsers.addAll(leaderGrade2);
        leaderGradeUsers.addAll(leaderGrade3);
        leaderGradeUsers.addAll(leaderGrade4);

        return UserPrivilegedResponseDto.builder()
                .presidentUser(presidentUser)
                .vicePresidentUser(vicePresidentUser)
                .councilUsers(councilUsers)
                .leaderGradeUsers(leaderGradeUsers)
                .leaderCircleUsers(leaderCircleUsers)
                .leaderAlumni(leaderAlumniUser)
                .build();
    }
}
