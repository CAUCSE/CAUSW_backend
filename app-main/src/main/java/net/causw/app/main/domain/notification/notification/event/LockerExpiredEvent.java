package net.causw.app.main.domain.notification.notification.event;

public record LockerExpiredEvent(String userId, String lockerId) {
}
