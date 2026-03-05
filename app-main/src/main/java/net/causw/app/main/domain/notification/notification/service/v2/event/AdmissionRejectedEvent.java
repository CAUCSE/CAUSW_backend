package net.causw.app.main.domain.notification.notification.service.v2.event;

import net.causw.app.main.domain.user.account.entity.user.User;

public record AdmissionRejectedEvent(User targetUser, String rejectMessage) {
}
