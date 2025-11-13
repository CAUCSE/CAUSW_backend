package net.causw.app.main.api.dto.user;

import org.springframework.data.domain.Page;

import net.causw.app.main.api.dto.comment.CommentsOfUserResponseDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserCommentsResponseDto {
	private String id;
	private String email;
	private String name;
	private String studentId;
	private Integer admissionYear;
	private String profileImageUrl;
	private Page<CommentsOfUserResponseDto> comment;

}
