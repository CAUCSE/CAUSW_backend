package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
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

}
