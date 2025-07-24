package net.causw.app.main.dto.comment;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommentsOfUserResponseDto {
	private String id;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Boolean isDeleted;
	private String boardId;
	private String boardName;
	private String postId;
	private String postName;
	private String circleId;
	private String circleName;

}
