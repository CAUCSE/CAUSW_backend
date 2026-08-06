package net.causw.app.main.domain.notification.notification.event;

public record PostLikeMilestoneReachedEvent(String postId, String likerId, long milestoneCount) {
}
