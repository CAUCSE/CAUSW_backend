package net.causw.app.main.dto.user;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserPostResponseDto {
	private String id;
	private String title;
	private String boardId;
	private String boardName;
	private String circleId;
	private String circleName;
	private Long numComment;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
