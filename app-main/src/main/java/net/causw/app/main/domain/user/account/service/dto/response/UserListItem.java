package net.causw.app.main.domain.user.account.service.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserListItem(
        String id,
        String name,
        String studentId,
        Department department,
        UserState state,
        AcademicStatus academicStatus,
        LocalDateTime createdAt
) {
    public static UserListItem from(User user) {
        return new UserListItem(
                user.getId(),
                user.getName(),
                user.getStudentId(),
                user.getDepartment(),
                user.getState(),
                user.getAcademicStatus(),
                user.getCreatedAt()
        );
    }
}
