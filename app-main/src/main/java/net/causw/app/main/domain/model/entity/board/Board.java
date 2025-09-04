package net.causw.app.main.domain.model.entity.board;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.ColumnDefault;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.user.RoleGroup;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_board")
public class Board extends BaseEntity {
	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", nullable = true)
	private String description;

	@Column(name = "create_role_list", nullable = false)
	private String createRoles;

	@Column(name = "category", nullable = false)
	private String category;

	@Setter
	@Column(name = "is_deleted", nullable = false)
	@ColumnDefault("false")
	private Boolean isDeleted;

	@Column(name = "is_default", nullable = false)
	@ColumnDefault("false")
	private Boolean isDefault;

	@Column(name = "is_alumni", nullable = false)
	@ColumnDefault("false")
	private Boolean isAlumni;

	@Column(name = "is_home", nullable = false)
	@ColumnDefault("false")
	private Boolean isHome;

	@Column(name = "is_anonymous_allowed", nullable = false)
	@ColumnDefault("false")
	private Boolean isAnonymousAllowed;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "circle_id", nullable = true)
	private Circle circle;

	@OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	private Set<Post> postSet;

	@Column(name = "is_default_notice", nullable = false)
	@ColumnDefault("false")
	private Boolean isDefaultNotice; // 모두에게 알림이 가야 하는

	public static Board of(
		String name,
		String description,
		String category,
		Boolean isAnonymousAllowed,
		Circle circle
	) {
		Set<String> roleSet = Arrays.stream(Role.values()) // 일반 게시판 생성시 글쓰기 권한 '모두 허용'
			.map(Role::getValue)
			.collect(Collectors.toSet());

		roleSet.remove(Role.NONE.getValue()); // 비회원 글쓰기 권한 제한

		String createRoles = String.join(",", roleSet);

		return Board.builder()
			.name(name)
			.description(description)
			.createRoles(createRoles)
			.category(category)
			.isDeleted(false)
			.isDefault(false)
			.isAnonymousAllowed(isAnonymousAllowed)
			.circle(circle)
			.postSet(new HashSet<>())
			.isDefaultNotice(false)
			.isAlumni(false) //FIXME : 크자회 서비스의 게시글 생성 신청 구현시 변경
			.isHome(false)
			.build();
	}

	public static Board createNoticeBoard(
		String name,
		String description,
		List<String> createRoleList,
		String category,
		Boolean isAnonymousAllowed,
		Boolean isAlumni,
		Circle circle
	) {
		Set<String> roleSet = RoleGroup.EXECUTIVES.getRoles().stream() // 집행부(관리자, 학생회장, 부학생회장) 글쓰기 권한 보장
			.map(Role::getValue)
			.collect(Collectors.toSet());

		if (createRoleList != null) {
			roleSet.addAll(
				createRoleList.stream() // 공지 게시판 생성시 글쓰기 권한 '선택적 허용'
					.map(Role::of)
					.map(Role::getValue)
					.collect(Collectors.toSet())
			);
		}

		roleSet.remove(Role.NONE.getValue()); // 비회원 글쓰기 권한 제한

		String createRoles = String.join(",", roleSet);

		return Board.builder()
			.name(name)
			.description(description)
			.createRoles(createRoles)
			.category(category)
			.isDeleted(false)
			.isDefault(false)
			.isAnonymousAllowed(isAnonymousAllowed)
			.circle(circle)
			.postSet(new HashSet<>())
			.isDefaultNotice(false)
			.isAlumni(isAlumni != null ? isAlumni : false)
			.isHome(false)
			.build();
	}

	public void update(String name, String description, String createRoles, String category) {
		this.name = name;
		this.description = description;
		this.createRoles = createRoles;
		this.category = category;
	}
}
