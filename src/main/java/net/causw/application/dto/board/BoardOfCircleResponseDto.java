package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.board.Board;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.post.PostDomainModel;
import net.causw.domain.model.enums.Role;

import java.time.LocalDateTime;
import java.util.Arrays;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BoardOfCircleResponseDto {


    @Schema(description = "게시판 id 값", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "게시판 이름", example = "board_example")
    private String name;

    @Schema(description = "작성 가능 여부", example = "true")
    private Boolean writable;

    @Schema(description = "삭제 여부", example = "false")
    private Boolean isDeleted;

    @Schema(description = "게시글 id", example = "uuid 형식의 String 값입니다.")
    private String postId;

    @Schema(description = "게시글 제목", example = "post_title_example")
    private String postTitle;

    @Schema(description = "게시글 작성자 이름", example = "post_writer_example")
    private String postWriterName;

    @Schema(description = "게시글 작성자 id", example = "uuid 형식의 String 값입니다.")
    private String postWriterStudentId;

    @Schema(description = "게시글 생성 시간", example =  "2024-01-26T18:40:40.643Z")
    private LocalDateTime postCreatedAt;

    @Schema(description = "게시글 댓글 개수", example =  "12")
    private Long postNumComment;
    // FIXME: Port 분리 후 삭제 필요
    public static BoardOfCircleResponseDto from(
            BoardDomainModel boardDomainModel,
            Role userRole,
            PostDomainModel postDomainModel,
            Long numComment
    ) {
        return BoardOfCircleResponseDto.builder()
                .id(boardDomainModel.getId())
                .name(boardDomainModel.getName())
                .writable(boardDomainModel.getCreateRoleList().stream().anyMatch(str -> userRole.getValue().contains(str)))
                .isDeleted(boardDomainModel.getIsDeleted())
                .postId(postDomainModel.getId())
                .postTitle(postDomainModel.getTitle())
                .postWriterName(postDomainModel.getWriter().getName())
                .postWriterStudentId(postDomainModel.getWriter().getStudentId())
                .postCreatedAt(postDomainModel.getCreatedAt())
                .postNumComment(numComment)
                .build();
    }

    public static BoardOfCircleResponseDto from(
            BoardDomainModel boardDomainModel,
            Role userRole
    ) {
        return BoardOfCircleResponseDto.builder()
                .id(boardDomainModel.getId())
                .name(boardDomainModel.getName())
                .writable(boardDomainModel.getCreateRoleList().stream().anyMatch(str -> userRole.getValue().contains(str)))
                .isDeleted(boardDomainModel.getIsDeleted())
                .postNumComment(0L)
                .build();
    }

    // Board의 CreateRoles는 List가 ","로 이어진 형태로 존재. "," 기준으로 split해서 List<String>으로 변환 후 userRole과 비교
    public static Boolean isWriteable(Board board, Role userRole) {
        return Arrays.stream(
                        board.getCreateRoles().split(","))
                .anyMatch(str ->
                        userRole.getValue().contains(str)
                );
    }
}
