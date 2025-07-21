package net.causw.app.main.domain.model.entity.board;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.enums.user.Role;
import org.hibernate.annotations.ColumnDefault;

import java.util.*;
import java.util.stream.Collectors;

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

    @Column(name = "is_anonymous_allowed", nullable = false)
    @ColumnDefault("false")
    private Boolean is_anonymous_allowed;

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
            List<String> createRoleList,
            String category,
            Boolean is_anonymous_allowed,
            Circle circle
    ) {
        if (createRoleList != null) {
            if (createRoleList.isEmpty()) {
                createRoleList.add(Role.ADMIN.getValue());
                createRoleList.add(Role.PRESIDENT.getValue());
                createRoleList.add(Role.VICE_PRESIDENT.getValue());
            } else if (createRoleList.contains("ALL")) {
                createRoleList.addAll(
                        Arrays.stream(Role.values())
                                .map(Role::getValue)
                                .toList()
                );
                createRoleList.remove(Role.NONE.getValue());
                createRoleList.remove("ALL");
            } else {
                createRoleList = createRoleList
                        .stream()
                        .map(Role::of)
                        .map(Role::getValue)
                        .collect(Collectors.toList());
                createRoleList.remove(Role.NONE.getValue());
                if (!createRoleList.contains(Role.ADMIN.getValue())) {
                    createRoleList.add(Role.ADMIN.getValue());
                }
                if (!createRoleList.contains(Role.PRESIDENT.getValue())) {
                    createRoleList.add(Role.PRESIDENT.getValue());
                }
                if (!createRoleList.contains(Role.VICE_PRESIDENT.getValue())) {
                    createRoleList.add(Role.VICE_PRESIDENT.getValue());
                }
            }
        }

        return Board.builder()
                .name(name)
                .description(description)
                .createRoles(createRoleList == null ? "" :
                        String.join(",", createRoleList)
                )
                .category(category)
                .isDeleted(false)
                .isDefault(false)
                .is_anonymous_allowed(is_anonymous_allowed)
                .circle(circle)
                .postSet(new HashSet<>())
                .isDefaultNotice(false)
                .build();
    }

    public void update(String name, String description, String createRoles, String category){
        this.name = name;
        this.description = description;
        this.createRoles = createRoles;
        this.category = category;
    }
}
