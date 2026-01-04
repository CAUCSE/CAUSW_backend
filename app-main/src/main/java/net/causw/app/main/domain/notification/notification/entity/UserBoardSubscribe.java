package net.causw.app.main.domain.notification.notification.entity;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.entity.BaseEntity;

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
@Table(name = "tb_user_board_subscribe")
public class UserBoardSubscribe extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id")
	private Board board;

	@Column(name = "is_subscribed")
	private Boolean isSubscribed;

	public void setIsSubscribed(Boolean subscribed) {
		this.isSubscribed = subscribed;
	}

	public static UserBoardSubscribe of(
		User user,
		Board board,
		Boolean isSubscribed) {
		return UserBoardSubscribe.builder()
			.user(user)
			.board(board)
			.isSubscribed(isSubscribed)
			.build();
	}

}
