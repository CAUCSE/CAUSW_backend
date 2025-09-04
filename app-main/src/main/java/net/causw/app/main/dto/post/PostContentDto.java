package net.causw.app.main.dto.post;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostContentDto {
	@Schema(description = "게시글 제목")
	private String title;

	@Schema(description = "게시글 Id", example = "uuid 형식의 String 값입니다.")
	private String contentId;

	@Schema(description = "게시글 작성자 닉네임")
	private String writerNickname;

	@Schema(description = "표시될 게시글 작성자 닉네임", example = "[닉네임/비활성 유저/익명]")
	private String displayWriterNickname;

	@Schema(description = "게시글 생성 시간", example = "2024-01-26T18:40:40.643Z")
	private LocalDateTime createdAt;

	@Schema(description = "익명글 여부", example = "False")
	private Boolean isAnonymous;

	public void updateAnonymousPostContent() {
		if (Boolean.TRUE.equals(this.isAnonymous)) {
			this.writerNickname = null;
		}
	}
}