package net.causw.app.main.domain.community.post.api.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.shared.dto.ProfileImageDto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostResponse(
	@Schema(description = "게시글 id", example = "uuid 형식의 String 값입니다.") String id,

	@Schema(description = "게시글 내용", example = "안녕하세요. 학생회입니다. 공지사항입니다.") String content,

	@Schema(description = "게시글 삭제여부", example = "false") Boolean isDeleted,

	@Schema(description = "표시될 게시글 작성자 닉네임", example = "[닉네임/비활성 유저/익명]") String displayWriterNickname,

	@Schema(description = "게시글 작성자의 프로필 이미지 정보 (익명인 경우 null)") ProfileImageDto writerProfileImage,

	@Schema(description = "첨부파일", example = "첨부파일 url 작성") List<String> fileUrlList,

	@Schema(description = "답글 개수", example = "13") Long numComment,

	@Schema(description = "게시글 좋아요 개수", example = "10") Long numLike,

	@Schema(description = "게시글 즐겨찾기 개수", example = "11") Long numFavorite,

	@Schema(description = "게시글에 연결된 투표 ID (투표가 없으면 null)", example = "uuid 형식의 String 값") String voteId,

	@Schema(description = "익명글 여부", example = "False") Boolean isAnonymous,

	@Schema(description = "게시글 작성자 여부", example = "False") Boolean isOwner,

	@Schema(description = "로그인한 사용자가 좋아요를 이미 누른지 여부", example = "False") Boolean isPostLike,

	@Schema(description = "로그인한 사용자가 즐겨찾기를 이미 누른지 여부", example = "False") Boolean isPostFavorite,

	@Schema(description = "게시글 업데이트 가능여부", example = "true") Boolean updatable,

	@Schema(description = "게시글 삭제 가능여부", example = "true") Boolean deletable,

	@Schema(description = "게시글 생성 시간", example = "2024-01-26T18:40:40.643Z") LocalDateTime createdAt,

	@Schema(description = "게시글 업데이트 시간", example = "2024-01-26T18:40:40.643Z") LocalDateTime updatedAt,

	@Schema(description = "게시판 ID", example = "uuid 형식의 String 값입니다.") String boardId,

	@Schema(description = "게시판 이름", example = "게시판 이름입니다.") String boardName) {
}