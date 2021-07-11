package net.causw.domain.model;

import lombok.Getter;
import net.causw.infra.Board;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static BoardDomainModel of(Board board) {
        return new BoardDomainModel(
                board.getId(),
                board.getName(),
                board.getDescription(),
                new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))),
                new ArrayList<>(Arrays.asList(board.getModifyRoles().split(","))),
                new ArrayList<>(Arrays.asList(board.getReadRoles().split(",")))
        );
    }
}
