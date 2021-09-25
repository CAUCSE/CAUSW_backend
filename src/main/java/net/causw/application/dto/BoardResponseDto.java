package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.Board;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BoardResponseDto {
    private String id;
    private String name;
    private String description;
    private List<String> createRoleList;
    private List<String> modifyRoleList;
    private List<String> readRoleList;
    private Boolean isDeleted;

    private String circleId;
    private String circleName;

    private BoardResponseDto(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            List<String> modifyRoleList,
            List<String> readRoleList,
            Boolean isDeleted,
            String circleId,
            String circleName
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createRoleList = createRoleList;
        this.modifyRoleList = modifyRoleList;
        this.readRoleList = readRoleList;
        this.isDeleted = isDeleted;
        this.circleId = circleId;
        this.circleName = circleName;
    }

    public static BoardResponseDto from(Board board) {
        String circleId = null;
        String circleName = null;
        if (board.getCircle() != null) {
            circleId = board.getCircle().getId();
            circleName = board.getCircle().getName();
        }
        return new BoardResponseDto(
                board.getId(),
                board.getName(),
                board.getDescription(),
                new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))),
                new ArrayList<>(Arrays.asList(board.getModifyRoles().split(","))),
                new ArrayList<>(Arrays.asList(board.getReadRoles().split(","))),
                board.getIsDeleted(),
                circleId,
                circleName
        );
    }

    public static BoardResponseDto from(BoardDomainModel boardDomainModel) {
        String circleId = boardDomainModel.getCircle().map(CircleDomainModel::getId).orElse(null);
        String circleName = boardDomainModel.getCircle().map(CircleDomainModel::getName).orElse(null);

        return new BoardResponseDto(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getDescription(),
                boardDomainModel.getCreateRoleList(),
                boardDomainModel.getModifyRoleList(),
                boardDomainModel.getReadRoleList(),
                boardDomainModel.getIsDeleted(),
                circleId,
                circleName
        );
    }
}
