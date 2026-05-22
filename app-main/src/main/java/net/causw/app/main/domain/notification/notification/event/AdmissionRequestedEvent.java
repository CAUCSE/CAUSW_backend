package net.causw.app.main.domain.notification.notification.event;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;

public record AdmissionRequestedEvent(String requesterId, AcademicStatus targetStatus, String requestStudentId) {
}
