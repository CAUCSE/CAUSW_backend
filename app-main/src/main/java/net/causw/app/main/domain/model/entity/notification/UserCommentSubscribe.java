package net.causw.app.main.domain.model.entity.notification;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.user.User;

import jakarta.persistence.Column;
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
@Table(name = "tb_user_comment_subscribe")
public class UserCommentSubscribe extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id")
	private Comment comment;

	@Column(name = "is_subscribed")
	private Boolean isSubscribed;

	public static UserCommentSubscribe of(
		User user,
		Comment comment,
		Boolean isSubscribed
	) {
		return UserCommentSubscribe.builder()
			.user(user)
			.comment(comment)
			.isSubscribed(isSubscribed)
			.build();
	}

	public void setIsSubscribed(Boolean subscribed) {
		this.isSubscribed = subscribed;
	}
}
