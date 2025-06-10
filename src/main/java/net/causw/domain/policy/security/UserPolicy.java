package net.causw.domain.policy.security;

import net.causw.adapter.persistence.user.User;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import org.springframework.stereotype.Component;

@Component
public class UserPolicy {
    public static boolean isAcademicRecordCertified(User user) {
        AcademicStatus academicStatus = user.getAcademicStatus();

        if (academicStatus == null) {
            return false;
        } else return !academicStatus.equals(AcademicStatus.UNDETERMINED);
    }

    public static boolean isStateActive(CustomUserDetails userDetails) {
        return userDetails.getUserState() == UserState.ACTIVE;
    }
}
