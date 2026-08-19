package net.causw.app.main.domain.community.comment.service.dto;

import java.util.List;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.common.service.CommunityPermissionPolicy;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.dto.ProfileImageDto;
import net.causw.global.constant.StaticValue;

/**
 * 댓글·대댓글 작성자 정보 및 접근 권한을 담는 공통 DTO.
 *
 * <p>{@link CommentResult}에서 공통으로 사용합니다.
 * 익명 댓글이거나 탈퇴·비활성 유저인 경우 닉네임을 고정값으로 치환하고,
 * 개인 정보(이름, 입학연도, 프로필 이미지)를 노출하지 않습니다.</p>
 *
 * @param writerName            작성자 실명 (익명 댓글이면 {@code null})
 * @param writerNickname        작성자 닉네임 (익명 댓글이면 {@code null})
 * @param displayWriterNickname 화면에 표시되는 닉네임 (익명·탈퇴 시 고정 문자열로 치환)
 * @param writerAdmissionYear   작성자 입학연도 (익명 댓글이면 {@code null})
 * @param writerProfileImage    작성자 프로필 이미지 정보 (익명/차단/추방/탈퇴 시 GHOST)
 * @param updatable             현재 조회 유저가 이 댓글을 수정할 수 있는지 여부
 * @param deletable             현재 조회 유저가 이 댓글을 삭제할 수 있는지 여부
 * @param isBlocked             작성자가 현재 조회 유저에 의해 차단됐는지 여부
 * @param isAnonymous           익명 댓글 여부
 * @param isOwner               현재 조회 유저가 이 댓글의 작성자인지 여부
 */
public record CommentAuthorInfo(
	String writerName,
	String writerNickname,
	String displayWriterNickname,
	Integer writerAdmissionYear,
	ProfileImageDto writerProfileImage,
	Boolean updatable,
	Boolean deletable,
	Boolean isBlocked,
	Boolean isAnonymous,
	Boolean isOwner) {

	/**
	 * 댓글 작성자 정보와 현재 조회 유저의 권한을 조합해 {@code CommentAuthorInfo}를 생성합니다.
	 *
	 * <p>수정 권한은 작성자에게만 부여하며, 삭제 권한은 작성자와 정책상 moderation 주체에게 부여합니다.
	 * 탈퇴(INACTIVE) 또는 DROP 유저의 닉네임은 고정값으로 치환됩니다.</p>
	 *
	 * @param comment                  댓글 엔티티
	 * @param writerProfileImageEntity 댓글 작성자 프로필 이미지 엔티티
	 * @param isAnonymous              익명 댓글 여부
	 * @param currentUser              현재 조회 유저
	 * @param boardConfig              게시판 설정
	 * @param boardAdminIds            게시판 관리자 ID 목록
	 * @param isBlocked                작성자가 현재 유저에 의해 차단됐는지 여부
	 * @return 조합된 {@code CommentAuthorInfo}
	 */
	public static CommentAuthorInfo of(
		Comment comment, UserProfileImage writerProfileImageEntity, Boolean isAnonymous, User currentUser,
		BoardConfig boardConfig,
		List<String> boardAdminIds, boolean isBlocked) {
		User writer = comment.getWriter();
		boolean isOwner = writer != null && writer.getId().equals(currentUser.getId());
		boolean canEdit = CommunityPermissionPolicy.canUpdateComment(
			currentUser, comment, boardConfig, boardAdminIds);
		boolean canDelete = CommunityPermissionPolicy.canDeleteComment(
			currentUser, comment, boardConfig, boardAdminIds);

		boolean isInactiveUser = writer != null && (writer.isInactive() || writer.getState() == UserState.DROP);
		String displayWriterNickname;
		if (isInactiveUser) {
			displayWriterNickname = StaticValue.INACTIVE_USER_NICKNAME;
		} else if (Boolean.TRUE.equals(isAnonymous)) {
			displayWriterNickname = comment.getAnonymousNickname() != null
				? comment.getAnonymousNickname()
				: StaticValue.ANONYMOUS_USER_NICKNAME;
		} else {
			displayWriterNickname = writer != null ? writer.getNickname() : null;
		}

		String writerName = null;
		String writerNickname = null;
		Integer writerAdmissionYear = null;
		ProfileImageDto writerProfileImage;
		if (Boolean.TRUE.equals(isAnonymous) || writer == null) {
			// 익명 댓글이거나 작성자가 없는 경우 → GHOST 타입, url null
			writerProfileImage = ProfileImageDto.anonymous();
		} else {
			writerName = writer.getName();
			writerNickname = writer.getNickname();
			writerAdmissionYear = writer.getAdmissionYear();

			if (isBlocked) {
				// 차단된 유저는 GHOST 처리 (비식별)
				writerProfileImage = ProfileImageDto.forBlockedUser();
			} else {
				// 추방/탈퇴 유저는 ProfileImageDto.from()에서 GHOST 처리됨
				writerProfileImage = ProfileImageDto.from(writer, writerProfileImageEntity);
			}
		}

		return new CommentAuthorInfo(
			writerName, writerNickname, displayWriterNickname,
			writerAdmissionYear, writerProfileImage,
			canEdit, canDelete, isBlocked, isAnonymous, isOwner);
	}
}
