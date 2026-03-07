package net.causw.app.main.shared.infra.mail.event;

public record EmailVerificationEvent(String email, String verificationCode) {
}
