package net.causw.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.Board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BoardFullDto {
    private String id;
    private String name;
    private String description;
    private List<String> createRoleList;
    private List<String> modifyRoleList;
    private List<String> readRoleList;
    private Boolean isDeleted;

    private CircleFullDto circle;

    private BoardFullDto(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            List<String> modifyRoleList,
            List<String> readRoleList,
            Boolean isDeleted,
            CircleFullDto circle
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

    public static BoardFullDto from(Board board) {
        CircleFullDto circleFullDto = null;
        if (board.getCircle() != null) { circleFullDto = CircleFullDto.from(board.getCircle()); }
        return new BoardFullDto(
            board.getId(),
            board.getName(),
            board.getDescription(),
            new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))),
            new ArrayList<>(Arrays.asList(board.getModifyRoles().split(","))),
            new ArrayList<>(Arrays.asList(board.getReadRoles().split(","))),
            board.getIsDeleted(),
            circleFullDto
        );
    }
}
