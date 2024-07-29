package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardCreateRequestDto {

    @Schema(description = "게시판 이름", example = "board_name")
    @NotBlank(message = "게시판 이름을 입력해 주세요.")
    private String name;

    @Schema(description = "게시판 설명", example = "board_description")
    private String description;

    @Schema(description = "게시판에 글을 작성할 수 있는 권한 명단", example = "[ 'ADMIN' ]")
    @NotEmpty(message = "게시판 글에 작성할 수 있는 권한 명단을 입력해 주세요.")
    private List<String> createRoleList;

    @Schema(description = "게시판 카테고리", example = "APP_NOTICE")
    @NotBlank(message = "게시판 카테고리를 선택해 주세요.")
    private String category;

    @Schema(description = "게시판이 속한 동아리 id", example = "uuid 형식의 String 값입니다(nullable).")
    private String circleId;

    public Optional<String> getCircleId() {
        return Optional.ofNullable(this.circleId);
    }
}
