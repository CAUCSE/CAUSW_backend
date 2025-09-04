package net.causw.app.main.domain.model.entity.board;

import org.hibernate.annotations.ColumnDefault;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.board.BoardApplyStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tb_board_apply")
public class BoardApply extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "board_name", nullable = false)
	private String boardName;

	@Column(name = "description", nullable = true)
	private String description;

	@Column(name = "create_role_list", nullable = false)
	private String createRoles;

	@Column(name = "category", nullable = false)
	private String category;

	@Column(name = "accept_status", nullable = false)
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private BoardApplyStatus acceptStatus = BoardApplyStatus.AWAIT;

	@Column(name = "is_annonymous_allowed", nullable = false)
	@ColumnDefault("false")
	private Boolean isAnonymousAllowed;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "circle_id", nullable = true)
	private Circle circle;

	public static BoardApply of(
		User user,
		String boardName,
		String description,
		String category,
		Boolean isAnonymousAllowed,
		Circle circle
	) {
		// description 비어있을 경우 처리
		if (description == null) {
			description = "";
		}

		return BoardApply.builder()
			.user(user)
			.boardName(boardName)
			.description(description)
			.createRoles("ALL") // 모든 권한. 이렇게 넘기면 Board.of에서 List.of에 넣어서 일관된 처리
			.category(category)
			.acceptStatus(BoardApplyStatus.AWAIT)
			.isAnonymousAllowed(isAnonymousAllowed)
			.circle(circle)
			.build();
	}

	public void updateAcceptStatus(BoardApplyStatus status) {
		this.acceptStatus = status;
	}

}
