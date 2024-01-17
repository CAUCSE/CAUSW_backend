package net.causw.domain.model.board;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.enums.Role;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
public class BoardDomainModel {
    private String id;
    private String description;

    @NotBlank(message = "게시판 이름이 입력되지 않았습니다.")
    private String name;

    @NotNull(message = "게시글 생성 권한이 입력되지 않았습니다.")
    private List<String> createRoleList;

    @NotBlank(message = "카테고리가 입력되지 않았습니다.")
    private String category;

    @NotNull(message = "게시판 상태가 입력되지 않았습니다.")
    private Boolean isDeleted;

    private CircleDomainModel circle;

    private BoardDomainModel(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            String category,
            Boolean isDeleted,
            CircleDomainModel circle
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createRoleList = createRoleList;
        this.category = category;
        this.isDeleted = isDeleted;
        this.circle = circle;
    }

    public static BoardDomainModel of(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            String category,
            Boolean isDeleted,
            CircleDomainModel circle
    ) {
        return new BoardDomainModel(
                id,
                name,
                description,
                createRoleList,
                category,
                isDeleted,
                circle
        );
    }

    public static BoardDomainModel of(
            String name,
            String description,
            List<String> createRoleList,
            String category,
            CircleDomainModel circle
    ) {
        if (createRoleList != null) {
            if (createRoleList.isEmpty()) {
                createRoleList.addAll(
                        Arrays.stream(Role.values())
                                .map(Role::getValue)
                                .collect(Collectors.toList())
                );
                createRoleList.remove(Role.NONE.getValue());
            } else {
                createRoleList = createRoleList
                        .stream()
                        .map(Role::of)
                        .map(Role::getValue)
                        .collect(Collectors.toList());

                createRoleList.add(Role.ADMIN.getValue());
            }
        }

        return new BoardDomainModel(
                null,
                name,
                description,
                createRoleList,
                category,
                false,
                circle
        );
    }

    public void update(
            String name,
            String description,
            List<String> createRoleList,
            String category
    ) {
        this.name = name;
        this.description = description;
        this.createRoleList = createRoleList;
        this.category = category;
    }

    public Optional<CircleDomainModel> getCircle() {
        return Optional.ofNullable(this.circle);
    }
}
