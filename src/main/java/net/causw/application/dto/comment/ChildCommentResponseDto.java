package net.causw.application.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.comment.ChildComment;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChildCommentResponseDto {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private String tagUserName;
    private String refChildComment;
    private String writerName;
    private Integer writerAdmissionYear;
    private String writerProfileImage;
    private Boolean updatable;
    private Boolean deletable;

    // FIXME: 리팩토링 후 삭제예정
    public static ChildCommentResponseDto of(
            ChildComment comment,
            boolean updatable,
            boolean deletable
    ) {
        return ChildCommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isDeleted(comment.getIsDeleted())
                .tagUserName(comment.getTagUserName())
                .refChildComment(comment.getRefChildComment())
                .writerName(comment.getWriter().getName())
                .writerAdmissionYear(comment.getWriter().getAdmissionYear())
                .writerProfileImage(comment.getWriter().getProfileImage())
                .updatable(updatable)
                .deletable(deletable)
                .build();
    }
}
