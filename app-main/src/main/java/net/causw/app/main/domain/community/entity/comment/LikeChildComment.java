package net.causw.app.main.domain.community.entity.comment;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_like_child_comment")
public class LikeChildComment extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "child_comment_id")
	private ChildComment childComment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public static LikeChildComment of(ChildComment childComment, User user) {
		return LikeChildComment.builder()
			.childComment(childComment)
			.user(user)
			.build();
	}

}
