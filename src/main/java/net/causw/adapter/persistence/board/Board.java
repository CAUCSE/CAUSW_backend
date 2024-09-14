package net.causw.adapter.persistence.board;

import lombok.*;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.Role;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    private Boolean isDeleted;

    @Column(name = "is_default", nullable = false)
    @ColumnDefault("false")
    private Boolean isDefault;

    @Column(name = "is_anonymous_allowed", nullable = false)
    @ColumnDefault("false")
    private Boolean is_anonymous_allowed;

    @ManyToOne
    @JoinColumn(name = "circle_id", nullable = true)
    private Circle circle;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private Set<Post> postSet;


    private Board(
            String id,
            String name,
            String description,
            String createRoles,
            String category,
            Boolean isDeleted,
            Circle circle
    ) {
        super(id);
        this.name = name;
        this.description = description;
        this.createRoles = createRoles;
        this.category = category;
        this.isDeleted = isDeleted;
        this.circle = circle;
    }

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
                createRoleList.add(Role.ADMIN.getValue());
                createRoleList.add(Role.PRESIDENT.getValue());
            }
        }

        return Board.builder()
                .name(name)
                .description(description)
                .createRoles(String.join(",", createRoleList))
                .category(category)
                .isDeleted(false)
                .isDefault(false)
                .is_anonymous_allowed(is_anonymous_allowed)
                .circle(circle)
                .postSet(new HashSet<>())
                .build();
    }

    public void setIsDeleted(boolean isDeleted){
        this.isDeleted = isDeleted;
    }

    public void update(String name, String description, String createRoles, String category){
        this.name = name;
        this.description = description;
        this.createRoles = createRoles;
        this.category = category;
    }
}
