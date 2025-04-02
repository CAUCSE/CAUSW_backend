package net.causw.application.dto.notification;


import lombok.Builder;
import lombok.Getter;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.post.Post;

@Getter
@Builder
public class BoardNotificationDto {
    private String title;
    private String body;

    public static BoardNotificationDto of(Board board, Post post) {
        return BoardNotificationDto.builder()
                .title(String.format("%s",
                        board.getName()
                        ))
                .body(String.format("새 게시글 : %s",
                        post.getTitle()))
                .build();
    }
}
