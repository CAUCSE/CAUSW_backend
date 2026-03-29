package net.causw.app.main.domain.notification.notification.event;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;

public record AdmissionRequestedEvent(User requester, AcademicStatus targetStatus) {
}
