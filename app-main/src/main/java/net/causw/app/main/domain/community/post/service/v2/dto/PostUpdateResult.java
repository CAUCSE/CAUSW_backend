package net.causw.app.main.domain.community.post.service.v2.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record PostUpdateResult(
	@Schema(description = "게시글 id", example = "uuid 형식의 String 값입니다.") String id,

	@Schema(description = "게시글 내용", example = "수정된 게시글 내용입니다.") String content,

	@Schema(description = "작성자 Id", example = "uuid-uuid") String writerId,

	@Schema(description = "첨부파일", example = "첨부파일 url 작성") List<String> fileUrlList,

	@Schema(description = "익명글 여부", example = "False") Boolean isAnonymous,

	@Schema(description = "게시글 생성 시간", example = "2024-01-26T18:40:40.643Z") LocalDateTime createdAt,

	@Schema(description = "게시글 업데이트 시간", example = "2024-01-26T18:40:40.643Z") LocalDateTime updatedAt,

	@Schema(description = "게시판 이름", example = "게시판 이름입니다.") String boardName) {

}
