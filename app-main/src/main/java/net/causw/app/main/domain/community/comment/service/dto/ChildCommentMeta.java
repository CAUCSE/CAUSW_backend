package net.causw.app.main.domain.community.comment.service.dto;

import java.util.List;

/**
 * 대댓글 응답 DTO 조립에 필요한 pre-fetch 데이터.
 *
 * <p>Service에서 조회한 데이터를 {@link net.causw.app.main.domain.community.comment.service.implementation.ChildCommentMapper}로 전달하는 역할을 합니다.
 * 대댓글 단건 작업(생성·수정·삭제)에서 사용됩니다.</p>
 *
 * @param boardAdminIds 게시판 관리자 ID 목록 (수정·삭제 권한 계산에 사용)
 * @param numLike       이 대댓글의 좋아요 수
 * @param isLiked       현재 조회 유저가 이 대댓글에 좋아요를 눌렀는지 여부
 * @param isBlocked     이 대댓글 작성자가 현재 조회 유저에 의해 차단됐는지 여부
 */
public record ChildCommentMeta(
	List<String> boardAdminIds,
	long numLike,
	boolean isLiked,
	boolean isBlocked) {
}
