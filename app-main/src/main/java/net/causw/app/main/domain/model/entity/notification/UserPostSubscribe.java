package net.causw.app.main.domain.model.entity.notification;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.post.Post;
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
@Table(name = "tb_user_post_subscribe")
public class UserPostSubscribe extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;

	@Column(name = "is_subscribed")
	private Boolean isSubscribed;

	public void setIsSubscribed(Boolean subscribed) {
		this.isSubscribed = subscribed;
	}

	public static UserPostSubscribe of(
		User user,
		Post post,
		Boolean isSubscribed
	) {
		return UserPostSubscribe.builder()
			.user(user)
			.post(post)
			.isSubscribed(isSubscribed)
			.build();
	}
}
