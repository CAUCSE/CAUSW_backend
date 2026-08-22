package net.causw.app.main.domain.community.board.entity;

import java.util.HashSet;
import java.util.Set;

import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.shared.entity.AuditableEntity;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class BoardConfig extends AuditableEntity {

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

	@Column(name = "official_nickname")
	private String officialNickname;

	@Column(name = "official_profile_image_id")
	private String officialProfileImageId;

	@ElementCollection(fetch = FetchType.LAZY)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "tb_board_config_department", joinColumns = @JoinColumn(name = "board_config_id"))
	@Column(name = "department")
	@BatchSize(size = 100)
	@Builder.Default
	private Set<Department> departments = new HashSet<>();

	public static BoardConfig of(String boardId, boolean isAnonymous, BoardReadScope readScope,
		BoardWriteScope writeScope, boolean isNotice, BoardVisibility visibility, int displayOrder,
		String officialNickname, String officialProfileImageId) {
		return of(boardId, isAnonymous, readScope, writeScope, isNotice, visibility, displayOrder,
			officialNickname, officialProfileImageId, Set.of());
	}

	public static BoardConfig of(String boardId, boolean isAnonymous, BoardReadScope readScope,
		BoardWriteScope writeScope, boolean isNotice, BoardVisibility visibility, int displayOrder,
		String officialNickname, String officialProfileImageId, Set<Department> departments) {
		return BoardConfig.builder()
			.boardId(boardId)
			.isAnonymous(isAnonymous)
			.readScope(readScope)
			.writeScope(writeScope)
			.isNotice(isNotice)
			.visibility(visibility)
			.displayOrder(displayOrder)
			.officialNickname(officialNickname)
			.officialProfileImageId(officialProfileImageId)
			.departments(departments == null ? new HashSet<>() : new HashSet<>(departments))
			.build();
	}

	public void update(boolean isAnonymous, BoardReadScope readScope, BoardWriteScope writeScope,
		boolean isNotice, BoardVisibility visibility, String officialNickname, String officialProfileImageId,
		Set<Department> departments) {
		this.isAnonymous = isAnonymous;
		this.readScope = readScope;
		this.writeScope = writeScope;
		this.isNotice = isNotice;
		this.visibility = visibility;
		this.officialNickname = officialNickname;
		this.officialProfileImageId = officialProfileImageId;
		this.departments.clear();
		if (departments != null) {
			this.departments.addAll(departments);
		}
	}

	public void updateDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
}
