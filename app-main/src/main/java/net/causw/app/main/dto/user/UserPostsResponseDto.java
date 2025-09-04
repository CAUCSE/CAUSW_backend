package net.causw.app.main.dto.user;

import org.springframework.data.domain.Page;

import net.causw.app.main.dto.post.PostsResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserPostsResponseDto {
	private String id;
	private String email;
	private String name;
	private String studentId;
	private Integer admissionYear;
	private String profileImageUrl;

	@Schema(description = "간락화된 게시글 정보들입니다")
	private Page<PostsResponseDto> posts;

}