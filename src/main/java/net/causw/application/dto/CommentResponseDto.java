package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.CommentDomainModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class CommentResponseDto {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private String postId;
    private String writerId;
    private String writerName;
    private String writerProfileImage;
    private String parentCommentId;
    private List<CommentResponseDto> childCommentList;

    private CommentResponseDto(
            String id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            String postId,
            String writerId,
            String writerName,
            String writerProfileImage,
            String parentCommentId,
            List<CommentResponseDto> childCommentList
    ) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.postId = postId;
        this.writerId = writerId;
        this.writerName = writerName;
        this.writerProfileImage = writerProfileImage;
        this.parentCommentId = parentCommentId;
        this.childCommentList = childCommentList;
    }

    public static CommentResponseDto from(CommentDomainModel comment) {
        String parentCommentId = null;
        if (comment.getParentComment() != null) {
            parentCommentId = comment.getParentComment().getId();
        }

        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getIsDeleted(),
                comment.getPostId(),
                comment.getWriter().getId(),
                comment.getWriter().getName(),
                comment.getWriter().getProfileImage(),
                parentCommentId,
                comment.getChildCommentList()
                        .stream()
                        .map(CommentResponseDto::from)
                        .collect(Collectors.toList())
        );
    }
}
