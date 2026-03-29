package net.causw.app.main.domain.notification.notification.event;

public record CommentChildCommentCreatedEvent(String commentId, String childCommentId) {
}
