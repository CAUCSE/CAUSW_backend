package net.causw.app.main.dto.user;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
	private List<UserResponseDto> alumniManager;
}
