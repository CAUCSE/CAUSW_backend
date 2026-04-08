package net.causw.app.main.domain.notification.notification.event;

public record AdmissionRejectedEvent(String adminId, String targetUserId, String rejectMessage) {
}
