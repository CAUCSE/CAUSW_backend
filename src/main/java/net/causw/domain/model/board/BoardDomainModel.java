package net.causw.domain.model.board;

import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.enums.Role;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Builder
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

    public static BoardDomainModel of(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            String category,
            Boolean isDeleted,
            CircleDomainModel circle
    ) {
        return BoardDomainModel.builder()
                .id(id)
                .name(name)
                .description(description)
                .createRoleList(createRoleList)
                .category(category)
                .isDeleted(isDeleted)
                .circle(circle)
                .build();
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
                createRoleList.add(Role.ADMIN.getValue());
                createRoleList.add(Role.PRESIDENT.getValue());
            } else if (createRoleList.contains("ALL")) {
                createRoleList.addAll(
                        Arrays.stream(Role.values())
                                .map(Role::getValue)
                                .collect(Collectors.toList())
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

        return BoardDomainModel.builder()
                .name(name)
                .description(description)
                .createRoleList(createRoleList)
                .category(category)
                .circle(circle)
                .build();
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
