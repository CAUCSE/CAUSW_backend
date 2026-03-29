package net.causw.app.main.domain.notification.notification.event;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;

public record OfficialPostEvent(Board board, Post post) {
}
