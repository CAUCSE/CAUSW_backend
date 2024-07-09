package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.enums.Role;

import java.util.List;

@Getter
@Setter
public class BoardResponseDto {

    @Schema(description = "게시판 id 값", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "게시판 이름", example = "board_example")
    private String name;

    @Schema(description = "게시판 설명", example = "board_description")
    private String description;

    @Schema(description = "게시판에 글을 작성할 수 있는 권한 명단", example = "[ 'ADMIN' ]")
    private List<String> createRoleList;

    @Schema(description = "게시판 카테고리", example = "APP_NOTICE")
    private String category;

    @Schema(description = "작성 가능 여부(미완)", example = "true")
    private Boolean writable;

    @Schema(description = "삭제 여부", example = "false")
    private Boolean isDeleted;

    @Schema(description = "게시판이 속한 동아리 id", example = "uuid 형식의 String 값입니다(nullable).")
    private String circleId;

    @Schema(description = "속한 동아리 이름", example = "circleName_example")
    private String circleName;

    // Private constructor to prevent instantiation from outside
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

    // Factory method to create BoardResponseDto from BoardDomainModel
    public static BoardResponseDto from(BoardDomainModel boardDomainModel, Role userRole) {
        String circleId = boardDomainModel.getCircle().map(CircleDomainModel::getId).orElse(null);
        String circleName = boardDomainModel.getCircle().map(CircleDomainModel::getName).orElse(null);

        return new BoardResponseDto(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getDescription(),
                boardDomainModel.getCreateRoleList(),
                boardDomainModel.getCategory(),
                boardDomainModel.getCreateRoleList().stream().anyMatch(str -> userRole.getValue().contains(str)),
                boardDomainModel.getIsDeleted(),
                circleId,
                circleName
        );
    }
}
