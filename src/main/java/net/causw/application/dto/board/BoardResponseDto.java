package net.causw.application.dto.board;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.enums.Role;

import java.util.List;

@Getter
@Setter
@Builder
public class BoardResponseDto {
    @ApiModelProperty(value = "게시판 id 값", example = "uuid 형식의 String 값입니다.")
    private String id;

    @ApiModelProperty(value = "게시판 이름", example = "board_example")
    private String name;

    @ApiModelProperty(value = "게시판 설명", example = "board_description")
    private String description;

    @ApiModelProperty(value = "게시판에 글을 작성할 수 있는 권한 명단", example = "[ 'ADMIN' ]")
    private List<String> createRoleList;

    @ApiModelProperty(value = "게시판 카테고리", example = "APP_NOTICE")
    private String category;

    @ApiModelProperty(value = "작성가능 여부(미완)", example = "true")
    private Boolean writable;

    @ApiModelProperty(value = "삭제 여부", example = "false")
    private Boolean isDeleted;

    @ApiModelProperty(value = "게시판이 속한 동아리 id", example = "uuid 형식의 String 값입니다(nullable).")
    private String circleId;

    @ApiModelProperty(value = "속한 동아리 이름", example = "circleName_example")
    private String circleName;

    public static BoardResponseDto from(BoardDomainModel boardDomainModel, Role userRole) {
        String circleId = boardDomainModel.getCircle().map(CircleDomainModel::getId).orElse(null);
        String circleName = boardDomainModel.getCircle().map(CircleDomainModel::getName).orElse(null);

        return BoardResponseDto.builder()
                .id(boardDomainModel.getId())
                .name(boardDomainModel.getName())
                .description(boardDomainModel.getDescription())
                .createRoleList(boardDomainModel.getCreateRoleList())
                .category(boardDomainModel.getCategory())
                .writable(boardDomainModel.getCreateRoleList().stream().anyMatch(str -> userRole.getValue().contains(str)))
                .isDeleted(boardDomainModel.getIsDeleted())
                .circleId(circleId)
                .circleName(circleName)
                .build();
    }
}