package net.causw.app.main.domain.community.post.api.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 목록 조회 응답")
public record PostListResponse(
	@Schema(description = "게시글 목록") List<PostItemResponse> posts,
	@Schema(description = "다음 커서 (createdAt|postId, 다음 페이지가 없으면 null)", example = "2026-02-09T12:00:00|post-id-123") String nextCursor) {

	@Schema(description = "게시글 아이템")
	public record PostItemResponse(
		@Schema(description = "게시글 ID") String postId,
		@Schema(description = "내용") String content,
		@Schema(description = "댓글 수") long numComment,
		@Schema(description = "좋아요 수") long numLike,
		@Schema(description = "즐겨찾기 수") long numFavorite,
		@Schema(description = "익명 여부") boolean isAnonymous,
		@Schema(description = "투표 ID (투표가 없으면 null)") String voteId,
		@Schema(description = "삭제 여부") boolean isDeleted,
		@Schema(description = "크롤링 게시글 여부") boolean isCrawled,
		@Schema(description = "작성자 닉네임 (익명인 경우 '익명')") String writerNickname,
		@Schema(description = "작성자 프로필 이미지 URL (익명인 경우 null)") String writerProfileImageUrl,
		@Schema(description = "생성일시") LocalDateTime createdAt,
		@Schema(description = "수정일시") LocalDateTime updatedAt,
		@Schema(description = "게시물 이미지 URL 목록") List<String> postImageUrls,
		@Schema(description = "게시판 ID") String boardId,
		@Schema(description = "게시판 이름") String boardName) {
	}
}
