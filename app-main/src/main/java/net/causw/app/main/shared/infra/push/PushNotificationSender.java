package net.causw.app.main.shared.infra.push;

import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;

public interface PushNotificationSender {

	void send(String token, String title, String body, PushNotificationData pushNotificationData) throws Exception;
}
