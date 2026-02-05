package net.causw.app.main.domain.community.post.api.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record PostCreateResponse(
	@Schema(description = "게시글 id", example = "uuid 형식의 String 값입니다.") String id,

	@Schema(description = "게시글 내용", example = "안녕하세요. 학생회입니다. 공지사항입니다.") String content,

	@Schema(description = "표시될 게시글 작성자 닉네임", example = "[닉네임/비활성 유저/익명]") String displayWriterNickname,

	@Schema(description = "게시글 작성자의 프로필 이미지", example = "프로필 이미지 url 작성") String writerProfileImage,

	@Schema(description = "첨부파일", example = "첨부파일 url 작성") List<String> fileUrlList,

	@Schema(description = "익명글 여부", example = "False") Boolean isAnonymous,

	@Schema(description = "게시글 생성 시간", example = "2024-01-26T18:40:40.643Z") LocalDateTime createdAt,

	@Schema(description = "게시글 업데이트 시간", example = "2024-01-26T18:40:40.643Z") LocalDateTime updatedAt,

	@Schema(description = "게시판 이름", example = "게시판 이름입니다.") String boardName) {

}
