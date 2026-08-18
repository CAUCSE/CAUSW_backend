package net.causw.app.main.domain.community.comment.entity;

import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 안에서 익명 댓글 작성자에게 부여된 랜덤 닉네임 매핑.
 *
 * <p>같은 게시글(postId) 안에서 같은 작성자(userId)는 항상 같은 닉네임을 재사용하고,
 * 서로 다른 작성자는 서로 다른 닉네임을 갖도록 유니크 제약으로 보장한다.</p>
 */
@Entity
@Table(name = "tb_comment_anonymous_nickname", uniqueConstraints = {
	@UniqueConstraint(name = "uq_comment_anonymous_nickname_post_user", columnNames = {"post_id", "user_id"}),
	@UniqueConstraint(name = "uq_comment_anonymous_nickname_post_nickname", columnNames = {"post_id", "nickname"})
})
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentAnonymousNickname extends BaseEntity {

	@Column(name = "post_id", nullable = false, updatable = false)
	private String postId;

	@Column(name = "user_id", nullable = false, updatable = false)
	private String userId;

	@Column(name = "nickname", nullable = false, updatable = false, length = 30)
	private String nickname;

	public static CommentAnonymousNickname of(String postId, String userId, String nickname) {
		return CommentAnonymousNickname.builder()
			.postId(postId)
			.userId(userId)
			.nickname(nickname)
			.build();
	}
}
