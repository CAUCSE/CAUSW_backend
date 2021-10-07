package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BoardResponseDto {
    private String id;
    private String name;
    private String description;
    private List<String> createRoleList;
    private String category;
    private Boolean isDeleted;

    private String circleId;
    private String circleName;

    private BoardResponseDto(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            String category,
            Boolean isDeleted,
            String circleId,
            String circleName
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createRoleList = createRoleList;
        this.category = category;
        this.isDeleted = isDeleted;
        this.circleId = circleId;
        this.circleName = circleName;
    }

    public static BoardResponseDto from(BoardDomainModel boardDomainModel) {
        String circleId = boardDomainModel.getCircle().map(CircleDomainModel::getId).orElse(null);
        String circleName = boardDomainModel.getCircle().map(CircleDomainModel::getName).orElse(null);

        return new BoardResponseDto(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getDescription(),
                boardDomainModel.getCreateRoleList(),
                boardDomainModel.getCategory(),
                boardDomainModel.getIsDeleted(),
                circleId,
                circleName
        );
    }
}
