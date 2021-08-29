package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class CommentFullDto {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private PostFullDto post;
    private UserFullDto writer;
    private List<CommentFullDto> childCommentList;

    private CommentFullDto(
            String id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            PostFullDto post,
            UserFullDto writer,
            List<CommentFullDto> childCommentList
    ) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.post = post;
        this.writer = writer;
        this.childCommentList = childCommentList;
    }

    public static CommentFullDto from(Comment comment) {
        return new CommentFullDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getIsDeleted(),
                PostFullDto.from(
                        comment.getPost()
                ),
                UserFullDto.from(
                        comment.getWriter()
                ),
                comment.getChildCommentList()
                        .stream()
                        .map(CommentFullDto::from)
                        .collect(Collectors.toList())
        );
    }
}
