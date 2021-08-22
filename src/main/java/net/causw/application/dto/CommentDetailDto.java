package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class CommentDetailDto {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private PostDetailDto post;
    private UserResponseDto writer;
    private List<CommentDetailDto> childCommentList;

    private CommentDetailDto(
            String id,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted,
            PostDetailDto post,
            UserResponseDto writer,
            List<CommentDetailDto> childCommentList
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

    public static CommentDetailDto from(Comment comment) {
        return new CommentDetailDto(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getIsDeleted(),
                PostDetailDto.from(
                        comment.getPost()
                ),
                UserResponseDto.from(
                        comment.getWriter()
                ),
                comment.getChildCommentList()
                        .stream()
                        .map(CommentDetailDto::from)
                        .collect(Collectors.toList())
        );
    }
}
