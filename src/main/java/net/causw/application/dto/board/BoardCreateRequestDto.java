package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCreateRequestDto {
    @NotBlank(message = "게시판 이름은 필수 입력 값입니다.")
    @Schema(description = "게시판 이름", example = "백준 질문 게시판", requiredMode = Schema.RequiredMode.REQUIRED)
    private String boardName;

    @Schema(description = "게시판 설명", example = "백준 문제에 대한 질문을 올리는 게시판입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "게시판 카테고리입니다. APP_NOTICE(공지게시판) / APP_FREE(일반 게시판) 둘 중에 하나 선택", example = "APP_NOTICE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "게시판 카테고리는 필수 입력 값입니다.")
    private String boardCategory;

    @NotEmpty(message = "권한 명단은 비어 있을 수 없습니다.")
    @Schema(description = "게시판에 글을 작성할 수 있는 권한 명단", example = "[ 'ADMIN' ] -> 배열로 받아야함", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@NotBlank String> createRoleList;

    @NotNull(message = "익명 허용 여부는 필수 입력 값입니다.")
    @Schema(description = "익명 허용 여부", example = "true -> boolean으로 받아야함", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isAnonymousAllowed;

    @Schema(description = "게시판이 속한 동아리 id입니다. 만약 동아리 게시판이 없을 시 null로 보내면 됩니다.", example = "uuid 형식의 String 값입니다(nullable).", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String circleId;
}
