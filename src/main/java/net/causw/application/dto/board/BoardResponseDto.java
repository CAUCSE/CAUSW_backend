package net.causw.application.dto.board;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.enums.Role;

import java.util.List;

@Getter
@Setter
public class BoardResponseDto {
    private String id;
    private String name;
    private String description;
    private List<String> createRoleList;
    private String category;
    private Boolean writable;
    private Boolean isDeleted;

    private String circleId;
    private String circleName;

    private BoardResponseDto(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            String category,
            Boolean writable,
            Boolean isDeleted,
            String circleId,
            String circleName
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createRoleList = createRoleList;
        this.category = category;
        this.writable = writable;
        this.isDeleted = isDeleted;
        this.circleId = circleId;
        this.circleName = circleName;
    }

    public static BoardResponseDto from(BoardDomainModel boardDomainModel, Role userRole) {
        String circleId = boardDomainModel.getCircle().map(CircleDomainModel::getId).orElse(null);
        String circleName = boardDomainModel.getCircle().map(CircleDomainModel::getName).orElse(null);

        return new BoardResponseDto(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getDescription(),
                boardDomainModel.getCreateRoleList(),
                boardDomainModel.getCategory(),
                boardDomainModel.getCreateRoleList().contains(userRole.getValue()),
                boardDomainModel.getIsDeleted(),
                circleId,
                circleName
        );
    }
}