package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.Comment;

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
    private PostFullDto post;
    private String writerId;
    private String writerName;
    private List<CommentResponseDto> childCommentList;

    private CommentResponseDto(
            String id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            PostFullDto post,
            String writerId,
            String writerName,
            List<CommentResponseDto> childCommentList
    ) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.post = post;
        this.writerId = writerId;
        this.writerName = writerName;
        this.childCommentList = childCommentList;
    }

    public static CommentResponseDto from(Comment comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getIsDeleted(),
                PostFullDto.from(
                        comment.getPost()
                ),
                comment.getWriter().getId(),
                comment.getWriter().getName(),
                comment.getChildCommentList()
                        .stream()
                        .map(CommentResponseDto::from)
                        .collect(Collectors.toList())
        );
    }
}
