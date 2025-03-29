package net.causw.application.dto.notification;

import lombok.Builder;
import lombok.Getter;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;

@Getter
@Builder
public class CommentNotificationDto {
    private String title;
    private String body;

    public static CommentNotificationDto of(Comment comment, ChildComment childComment) {
        return CommentNotificationDto.builder()
                .title(String.format("%s",
                        comment.getContent()
                ))
                .body(String.format("새 대댓글 : %s",
                        childComment.getContent()))
                .build();
    }
}
