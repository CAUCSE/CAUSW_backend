package net.causw.app.main.dto.user;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.model.enums.user.UserState;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserAdmissionsResponseDto {
	private String id;
	private String userName;
	private String userEmail;
	private Integer admissionYear;
	private List<String> attachImageUrlList;
	private String description;
	private UserState userState;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String studentId;

}
