package net.causw.app.main.domain.notification.notification.event;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;

public record CommentChildCommentCreatedEvent(Comment comment, ChildComment childComment) {
}
