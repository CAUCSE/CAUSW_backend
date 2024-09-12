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
}
