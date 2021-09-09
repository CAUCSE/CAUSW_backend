package net.causw.domain.model;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
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

    private BoardDomainModel(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            List<String> modifyRoleList,
            List<String> readRoleList
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createRoleList = createRoleList;
        this.modifyRoleList = modifyRoleList;
        this.readRoleList = readRoleList;
    }

    public static BoardDomainModel of(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            List<String> modifyRoleList,
            List<String> readRoleList
    ) {
        return new BoardDomainModel(
                id,
                name,
                description,
                createRoleList,
                modifyRoleList,
                readRoleList
        );
    }
}
