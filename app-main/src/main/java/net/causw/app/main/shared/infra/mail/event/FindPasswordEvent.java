package net.causw.app.main.shared.infra.mail.event;

public record FindPasswordEvent(String email, String newPassword) {
}
