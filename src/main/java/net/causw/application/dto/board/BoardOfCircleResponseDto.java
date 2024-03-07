package net.causw.application.dto.board;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.post.PostDomainModel;
import net.causw.domain.model.enums.Role;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardOfCircleResponseDto {

    @ApiModelProperty(value = "게시판 id 값", example = "uuid 형식의 String 값입니다.")
    private String id;

    @ApiModelProperty(value = "게시판 이름", example = "board_example")
    private String name;

    @ApiModelProperty(value = "작성 가능 여부", example = "true")
    private Boolean writable;

    @ApiModelProperty(value = "삭제 여부", example = "false")
    private Boolean isDeleted;

    @ApiModelProperty(value = "게시글 id", example = "uuid 형식의 String 값입니다.")
    private String postId;

    @ApiModelProperty(value = "게시글 제목", example = "post_title_example")
    private String postTitle;

    @ApiModelProperty(value = "게시글 작성자 이름", example = "post_writer_example")
    private String postWriterName;

    @ApiModelProperty(value = "게시글 작성자 id", example = "uuid 형식의 String 값입니다.")
    private String postWriterStudentId;

    @ApiModelProperty(value = "게시글 생성 시간", example =  "2024-01-26T18:40:40.643Z")
    private LocalDateTime postCreatedAt;

    @ApiModelProperty(value = "게시글 댓글 개수", example =  "12")
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
