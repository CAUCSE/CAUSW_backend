package net.causw.app.main.domain.community.post.api.v2.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "게시글 생성 요청. multipart/form-data 형식으로 'request' 파트(JSON)와 'images' 파트(파일 배열)로 구성됩니다.")
public record PostCreateRequest(
	@NotBlank(message = "게시글 내용을 입력해 주세요.")
	@Schema(description = "게시글 내용", example = "안녕하세요. 학생회입니다. 공지사항입니다.")
	String content,

	@NotBlank(message = "게시판 id를 입력해 주세요.")
	@Schema(description = "게시판 id", example = "uuid 형식의 String 값입니다.")
	String boardId,

	@NotNull(message = "익명글 여부를 선택해 주세요.")
	@Schema(description = "익명글 여부", example = "false")
	Boolean isAnonymous,

	@Valid
	@Schema(description = "이미지 메타데이터 목록. 각 항목의 fileIndex는 'images' 파트의 파일 배열 인덱스와 매핑됩니다.")
	List<ImageMeta> images) {

	@Schema(description = "게시글 생성 시 이미지 메타데이터")
	public record ImageMeta(
		@NotNull(message = "이미지 순서를 입력해 주세요.")
		@Schema(description = "최종 이미지 순서 (0부터 시작)", example = "0")
		Integer order,

		@NotNull(message = "파일 인덱스를 입력해 주세요.")
		@Schema(description = "'images' 파트의 파일 배열 인덱스", example = "0")
		Integer fileIndex,

		@NotNull(message = "대표 이미지 여부를 입력해 주세요.")
		@Schema(description = "대표 이미지 여부 (목록에서 정확히 1개만 true)", example = "true")
		Boolean isRepresentative) {
	}
}
