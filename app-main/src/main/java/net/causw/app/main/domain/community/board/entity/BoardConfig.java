package net.causw.app.main.domain.community.board.entity;

import net.causw.app.main.shared.entity.BaseEntity;
import net.causw.app.main.shared.entity.BaseEntityWithoutId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tb_board_config")
public class BoardConfig extends BaseEntityWithoutId {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;

	@Column(name = "board_id", nullable = false, unique = true)
	private String boardId;

	@Column(name = "is_anonymous", nullable = false)
	private boolean isAnonymous;

	@Column(name = "read_scope", nullable = false)
	@Enumerated(EnumType.STRING)
	private BoardReadScope readScope;

	@Column(name = "write_scope", nullable = false)
	@Enumerated(EnumType.STRING)
	private BoardWriteScope writeScope;

	@Column(name = "is_notice", nullable = false)
	private boolean isNotice;

	@Column(name = "visibility", nullable = false)
	@Enumerated(EnumType.STRING)
	private BoardVisibility visibility;

	@Column(name = "display_order", nullable = false)
	private Integer displayOrder;
}
