package net.causw.app.main.domain.community.comment.service.implementation;

import net.causw.app.main.domain.user.account.enums.user.ProfileImageType;

/**
 * 게시글 안에서 익명 댓글 작성자에게 부여된 닉네임과 프로필 이미지 타입의 짝.
 *
 * <p>동일 (게시글, 작성자) 조합은 항상 이 짝을 그대로 재사용한다.</p>
 */
public record CommentAnonymousIdentity(String nickname, ProfileImageType profileImageType) {
}
