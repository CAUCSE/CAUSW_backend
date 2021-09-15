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
    private PostResponseDto post;
    private String writerId;
    private String writerName;
    private List<CommentResponseDto> childCommentList;

    private CommentResponseDto(
            String id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            PostResponseDto post,
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

    public static CommentResponseDto from(CommentDomainModel comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getIsDeleted(),
                PostResponseDto.from(
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
