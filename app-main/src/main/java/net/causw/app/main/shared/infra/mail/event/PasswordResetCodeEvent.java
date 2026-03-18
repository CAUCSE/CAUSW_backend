package net.causw.app.main.shared.infra.mail.event;

public record PasswordResetCodeEvent(String email, String verificationCode) {
}
