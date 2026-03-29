package net.causw.app.main.domain.notification.notification.event;

import net.causw.app.main.domain.user.account.entity.user.User;

public record AdmissionRejectedEvent(User targetUser, String rejectMessage) {
}
