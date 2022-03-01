package net.causw.application.dto.board;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.Role;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardOfCircleResponseDto {
    private String id;
    private String name;
    private Boolean writable;
    private Boolean isDeleted;

    private String postId;
    private String postTitle;
    private String postWriterName;
    private String postWriterStudentId;
    private LocalDateTime postCreatedAt;
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
                boardDomainModel.getCreateRoleList().contains(userRole.getValue()),
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
                boardDomainModel.getCreateRoleList().contains(userRole.getValue()),
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
