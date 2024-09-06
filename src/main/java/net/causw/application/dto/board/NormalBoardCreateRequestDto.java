package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalBoardCreateRequestDto {
    @NotBlank(message = "게시판 이름은 필수 입력 값입니다.")
    @NotNull(message = "게시판 이름은 필수 입력 값입니다.")
    @Schema(description = "게시판 이름", example = "백준 질문 게시판")
    private String boardName;

    @Schema(description = "게시판 설명", example = "백준 문제에 대한 질문을 올리는 게시판입니다.")
    private String description;

    @NotBlank(message = "권한 명단은 필수 입력 값입니다.")
    @NotNull(message = "권한 명단은 필수 입력 값입니다.")
    @Schema(description = "게시판에 글을 작성할 수 있는 권한 명단", example = "[ 'ADMIN' ] -> 배열로 받아야함")
    private List<String> createRoleList;

    @NotBlank(message = "익명 허용 여부는 필수 입력 값입니다.")
    @NotNull(message = "익명 허용 여부는 필수 입력 값입니다.")
    @Schema(description = "익명 허용 여부", example = "true -> boolean으로 받아야함")
    private Boolean isAnonymous;
}
