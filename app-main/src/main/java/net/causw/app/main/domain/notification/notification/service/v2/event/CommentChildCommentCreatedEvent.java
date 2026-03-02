package net.causw.app.main.domain.notification.notification.service.v2.event;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;

public record CommentChildCommentCreatedEvent(Comment comment, ChildComment childComment) {
}
