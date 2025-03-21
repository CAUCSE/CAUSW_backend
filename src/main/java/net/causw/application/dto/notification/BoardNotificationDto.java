package net.causw.application.dto.notification;


import lombok.Builder;
import lombok.Getter;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.ceremony.Ceremony;
import net.causw.adapter.persistence.post.Post;

@Getter
@Builder
public class BoardNotificationDto {
    private String title;
    private String body;

    public static BoardNotificationDto of(Board board, Post post) {
        return BoardNotificationDto.builder()
                .title(String.format("[%s]",
                        post.getTitle().toString()
                        ))
                .body(String.format("%s",
                        board.getName()))
                .build();
    }
}
