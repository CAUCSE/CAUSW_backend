package net.causw.app.main.domain.notification.notification.event;

public record CeremonyRejectedEvent(String ceremonyId, String rejectReason) {
}