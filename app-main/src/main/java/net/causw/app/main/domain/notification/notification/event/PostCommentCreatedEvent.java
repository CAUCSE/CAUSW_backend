package net.causw.app.main.domain.notification.notification.event;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;

public record PostCommentCreatedEvent(Post post, Comment comment) {
}
