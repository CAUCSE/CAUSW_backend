package net.causw.app.main.domain.community.comment.service.dto;

import java.util.List;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.dto.ProfileImageDto;
import net.causw.global.constant.StaticValue;

/**
 * 댓글·대댓글 작성자 정보 및 접근 권한을 담는 공통 DTO.
 *
 * <p>{@link CommentResult}와 {@link ChildCommentResult}에서 공통으로 사용합니다.
 * 익명 댓글이거나 탈퇴·비활성 유저인 경우 닉네임을 고정값으로 치환하고,
 * 개인 정보(이름, 입학연도, 프로필 이미지)를 노출하지 않습니다.</p>
 *
 * @param writerName            작성자 실명 (익명 댓글이면 {@code null})
 * @param writerNickname        작성자 닉네임 (익명 댓글이면 {@code null})
 * @param displayWriterNickname 화면에 표시되는 닉네임 (익명·탈퇴 시 고정 문자열로 치환)
 * @param writerAdmissionYear   작성자 입학연도 (익명 댓글이면 {@code null})
 * @param writerProfileImage    작성자 프로필 이미지 정보 (익명 댓글이면 {@code null}, 차단/추방/탈퇴 시 GHOST)
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
	 * <p>수정·삭제 권한({@code updatable}/{@code deletable})은 작성자 본인이거나 게시판 관리자인 경우 부여됩니다.
	 * 탈퇴·비활성({@code INACTIVE}, {@code DROP}, {@code DELETED}) 유저의 닉네임은 고정값으로 치환됩니다.</p>
	 *
	 * @param writer       댓글 작성자 엔티티 (삭제된 경우 {@code null} 가능)
	 * @param isAnonymous  익명 댓글 여부
	 * @param currentUser  현재 조회 유저
	 * @param boardAdminIds 게시판 관리자 ID 목록
	 * @param isBlocked    작성자가 현재 유저에 의해 차단됐는지 여부
	 * @return 조합된 {@code CommentAuthorInfo}
	 */
	public static CommentAuthorInfo of(
		User writer, Boolean isAnonymous, User currentUser,
		List<String> boardAdminIds, boolean isBlocked) {
		boolean isOwner = writer != null && writer.getId().equals(currentUser.getId());
		boolean canEdit = isOwner || boardAdminIds.contains(currentUser.getId());

		boolean isInactiveUser = writer != null && List.of(UserState.INACTIVE, UserState.DROP, UserState.DELETED)
			.contains(writer.getState());
		String displayWriterNickname;
		if (isInactiveUser) {
			displayWriterNickname = StaticValue.INACTIVE_USER_NICKNAME;
		} else if (Boolean.TRUE.equals(isAnonymous)) {
			displayWriterNickname = StaticValue.ANONYMOUS_USER_NICKNAME;
		} else {
			displayWriterNickname = writer != null ? writer.getNickname() : null;
		}

		String writerName = null;
		String writerNickname = null;
		Integer writerAdmissionYear = null;
		ProfileImageDto writerProfileImage = null;
		if (!Boolean.TRUE.equals(isAnonymous) && writer != null) {
			writerName = writer.getName();
			writerNickname = writer.getNickname();
			writerAdmissionYear = writer.getAdmissionYear();

			if (isBlocked) {
				// 차단된 유저는 GHOST 처리 (비식별)
				writerProfileImage = ProfileImageDto.forBlockedUser();
			} else {
				// 추방/탈퇴 유저는 ProfileImageDto.from()에서 GHOST 처리됨
				writerProfileImage = ProfileImageDto.from(writer);
			}
		}

		return new CommentAuthorInfo(
			writerName, writerNickname, displayWriterNickname,
			writerAdmissionYear, writerProfileImage,
			canEdit, canEdit, isBlocked, isAnonymous, isOwner);
	}
}
