package net.causw.app.main.domain.community.board.entity;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "TB_FAVORITE_BOARD")
public class FavoriteBoard extends BaseEntity {
	@OneToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToOne
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	public static FavoriteBoard of(
		User user,
		Board board) {
		return FavoriteBoard.builder()
			.user(user)
			.board(board)
			.build();
	}
}
