package net.causw.app.main.domain.community.board.entity;

import net.causw.app.main.shared.entity.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "tb_board_admin")
public class BoardAdmin extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Column(name = "board_id", nullable = false)
	private String boardId;

	@Column(name = "user_id", nullable = false)
	private String userId;

	public static BoardAdmin of(String boardId, String userId) {
		return BoardAdmin.builder()
			.boardId(boardId)
			.userId(userId)
			.build();
	}
}
