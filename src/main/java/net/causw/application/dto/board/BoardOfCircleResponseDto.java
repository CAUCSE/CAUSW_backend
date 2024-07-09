package net.causw.application.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.post.PostDomainModel;
import net.causw.domain.model.enums.Role;

import java.time.LocalDateTime;

@Getter
@Setter
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

    private BoardOfCircleResponseDto(
            String id,
            String name,
            Boolean writable,
            Boolean isDeleted,
            String postId,
            String postTitle,
            String postWriterName,
            String postWriterStudentId,
            LocalDateTime postCreatedAt,
            Long postNumComment
    ) {
        this.id = id;
        this.name = name;
        this.writable = writable;
        this.isDeleted = isDeleted;
        this.postId = postId;
        this.postTitle = postTitle;
        this.postWriterName = postWriterName;
        this.postWriterStudentId = postWriterStudentId;
        this.postCreatedAt = postCreatedAt;
        this.postNumComment = postNumComment;
    }

    public static BoardOfCircleResponseDto from(
            BoardDomainModel boardDomainModel,
            Role userRole,
            PostDomainModel postDomainModel,
            Long numComment
    ) {
        return new BoardOfCircleResponseDto(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getCreateRoleList().stream().anyMatch(str -> userRole.getValue().contains(str)),
                boardDomainModel.getIsDeleted(),
                postDomainModel.getId(),
                postDomainModel.getTitle(),
                postDomainModel.getWriter().getName(),
                postDomainModel.getWriter().getStudentId(),
                postDomainModel.getCreatedAt(),
                numComment
        );
    }

    public static BoardOfCircleResponseDto from(
            BoardDomainModel boardDomainModel,
            Role userRole
    ) {
        return new BoardOfCircleResponseDto(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getCreateRoleList().stream().anyMatch(str -> userRole.getValue().contains(str)),
                boardDomainModel.getIsDeleted(),
                null,
                null,
                null,
                null,
                null,
                0L
        );
    }
}
