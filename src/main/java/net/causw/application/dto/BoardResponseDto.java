package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.Board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor
public class BoardResponseDto {
    private String id;
    private String name;
    private String description;
    private List<String> createRoleList;
    private List<String> modifyRoleList;
    private List<String> readRoleList;
    private Boolean isDeleted;

    private BoardResponseDto(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            List<String> modifyRoleList,
            List<String> readRoleList,
            Boolean isDeleted
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createRoleList = createRoleList;
        this.modifyRoleList = modifyRoleList;
        this.readRoleList = readRoleList;
        this.isDeleted = isDeleted;
    }

    public static BoardResponseDto from(Board board) {
        return new BoardResponseDto(
                board.getId(),
                board.getName(),
                board.getDescription(),
                new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))),
                new ArrayList<>(Arrays.asList(board.getModifyRoles().split(","))),
                new ArrayList<>(Arrays.asList(board.getReadRoles().split(","))),
                board.getIsDeleted()
        );
    }
}
