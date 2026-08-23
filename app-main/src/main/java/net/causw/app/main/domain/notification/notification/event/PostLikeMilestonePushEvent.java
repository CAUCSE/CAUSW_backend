package net.causw.app.main.domain.notification.notification.event;

import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;

public record PostLikeMilestonePushEvent(
	String recipientUserId,
	String pushTitle,
	String pushBody,
	PushNotificationData pushData) {
}
