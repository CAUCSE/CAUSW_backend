package net.causw.domain.model;

import lombok.Getter;
import java.util.List;

@Getter
public class BoardDomainModel {
    private String id;
    private String name;
    private String description;
    private List<String> createRoleList;
    private List<String> modifyRoleList;
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
