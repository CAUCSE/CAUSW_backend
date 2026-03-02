package net.causw.app.main.shared.infra.push;

public interface PushNotificationSender {

	void send(String token, String title, String body) throws Exception;
}
