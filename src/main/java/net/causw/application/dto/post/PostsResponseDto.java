package net.causw.application.dto.post;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.post.PostDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostsResponseDto {
    private String id;
    private String title;
    private String writerName;
    private Integer writerAdmissionYear;
    private Long numComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PostsResponseDto(
            String id,
            String title,
            String writerName,
            Integer writerAdmissionYear,
            Long numComment,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.writerName = writerName;
        this.writerAdmissionYear = writerAdmissionYear;
        this.numComment = numComment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PostsResponseDto from(
            PostDomainModel post,
            Long numComment
    ) {
        return new PostsResponseDto(
                post.getId(),
                post.getTitle(),
                post.getWriter().getName(),
                post.getWriter().getAdmissionYear(),
                numComment,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}