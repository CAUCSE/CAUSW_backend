package net.causw.app.main.domain.notification.notification.service.v2.event;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;

public record PostCommentCreatedEvent(Post post, Comment comment) {
}
