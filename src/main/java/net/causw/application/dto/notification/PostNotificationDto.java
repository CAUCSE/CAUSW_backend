package net.causw.application.dto.notification;

import lombok.Builder;
import lombok.Getter;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;

@Getter
@Builder
public class PostNotificationDto {
    private String title;
    private String body;

    public static PostNotificationDto of(Post post, Comment comment) {
        return PostNotificationDto.builder()
                .title(String.format("[%s]",
                        post.getTitle().toString()
                ))
                .body(String.format("%s",
                        comment.getContent()))
                .build();
    }
}
