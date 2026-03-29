package net.causw.app.main.domain.community.post.api.v2.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "게시글 수정 요청. multipart/form-data 형식으로 'request' 파트(JSON)와 'images' 파트(새 파일 배열)로 구성됩니다.")
public record PostUpdateRequest(
	@NotNull(message = "익명글 여부를 선택해 주세요.")
	@Schema(description = "익명글 여부", example = "false")
	Boolean isAnonymous,

	@NotBlank(message = "게시글 내용을 입력해 주세요.")
	@Schema(description = "게시글 내용", example = "수정된 게시글 내용입니다.")
	String content,

	@Valid
	@Schema(description = "이미지 메타데이터 목록. type=existing은 기존 이미지 유지(url 필수), type=new는 새 파일 업로드(fileIndex 필수)")
	List<ImageUpdateMeta> images) {

	@Schema(description = "게시글 수정 시 이미지 메타데이터")
	public record ImageUpdateMeta(
		@NotNull(message = "이미지 순서를 입력해 주세요.")
		@Schema(description = "최종 이미지 순서 (0부터 시작)", example = "0")
		Integer order,

		@NotNull(message = "이미지 타입을 입력해 주세요.")
		@Schema(description = "이미지 타입. existing: 기존 이미지 유지, new: 새 파일 업로드", example = "existing")
		ImageType type,

		@Schema(description = "기존 이미지 URL (type=existing 일 때 필수)", example = "https://cdn.example.com/img1.jpg")
		String url,

		@Schema(description = "'images' 파트의 파일 배열 인덱스 (type=new 일 때 필수)", example = "0")
		Integer fileIndex,

		@NotNull(message = "대표 이미지 여부를 입력해 주세요.")
		@Schema(description = "대표 이미지 여부 (목록에서 정확히 1개만 true)", example = "false")
		Boolean isRepresentative) {
	}

	public enum ImageType {
		@JsonProperty("existing")
		EXISTING,

		@JsonProperty("new")
		NEW
	}
}
