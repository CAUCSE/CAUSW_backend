package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.application.dto.user.UserResponseDto;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalBoardApplyResponseDto {
    @Schema(description = "게시판 신청 id 값", example = "uuid 형식의 String 값입니다.")
    private String id;

    @NotBlank(message = "게시판 이름은 필수 입력 값입니다.")
    @Schema(description = "게시판 이름", example = "백준 질문 게시판")
    private String boardName;

    @Schema(description = "게시판 설명", example = "백준 문제에 대한 질문을 올리는 게시판입니다.")
    private String description;

    @NotEmpty(message = "권한 명단은 비어 있을 수 없습니다.")
    @Schema(description = "게시판에 글을 작성할 수 있는 권한 명단(항상 ALL로 고정)", example = "ALL")
    private String createRoles;

    @NotNull(message = "익명 허용 여부는 필수 입력 값입니다.")
    @Schema(description = "익명 허용 여부", example = "true -> boolean으로 받아야함")
    private Boolean isAnonymousAllowed;

    @NotEmpty(message = "게시판 생성 신청자는 비어있을 수 없습니다.")
    @Schema(description = "게시판 생성 신청자", example = "게시판 생성 신청 User 객체를 반환합니다.")
    private UserResponseDto user;
}
