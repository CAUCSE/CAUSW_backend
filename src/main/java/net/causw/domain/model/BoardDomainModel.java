package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class BoardDomainModel {
    private String id;
    private String description;

    @NotBlank(message = "Name is blank")
    private String name;

    @NotNull(message = "Create role is null")
    private List<String> createRoleList;

    @NotNull(message = "Modify role is null")
    private List<String> modifyRoleList;

    @NotNull(message = "Read role is null")
    private List<String> readRoleList;

    @NotNull(message = "Board state is null")
    private Boolean isDeleted;

    private CircleDomainModel circle;

    private BoardDomainModel(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            List<String> modifyRoleList,
            List<String> readRoleList,
            Boolean isDeleted,
            CircleDomainModel circle
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createRoleList = createRoleList;
        this.modifyRoleList = modifyRoleList;
        this.readRoleList = readRoleList;
        this.isDeleted = isDeleted;
        this.circle = circle;
    }

    public static BoardDomainModel of(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            List<String> modifyRoleList,
            List<String> readRoleList,
            Boolean isDeleted,
            CircleDomainModel circle
    ) {
        return new BoardDomainModel(
                id,
                name,
                description,
                createRoleList,
                modifyRoleList,
                readRoleList,
                isDeleted,
                circle
        );
    }

    public static BoardDomainModel of(
            String name,
            String description,
            List<String> createRoleList,
            List<String> modifyRoleList,
            List<String> readRoleList,
            CircleDomainModel circle
    ) {
        return new BoardDomainModel(
                null,
                name,
                description,
                createRoleList,
                modifyRoleList,
                readRoleList,
                false,
                circle
        );
    }

    public Optional<CircleDomainModel> getCircle() {
        return Optional.ofNullable(this.circle);
    }
}
