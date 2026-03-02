package net.causw.app.main.domain.notification.notification.service.v2.event;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;

public record BoardPostCreatedEvent(Board board, Post post) {
}
