package net.causw.application.util;

import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.joinEntity.UserProfileImage;
import net.causw.adapter.persistence.vote.VoteRecord;
import net.causw.domain.model.enums.user.GraduationType;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class UserObjectBuilder extends User {

    public static User buildUser(
            String id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String email,
            String name,
            String phoneNumber,
            String password,
            String studentId,
            Integer admissionYear,
            String nickname,
            String major,
            AcademicStatus academicStatus,
            Integer currentCompletedSemester,
            Integer graduationYear,
            GraduationType graduationType,
            Set<Role> roles,
            UserProfileImage userProfileImage,
            UserState userState,
            Locker locker,
            List<CircleMember> circleMemberList,
            List<VoteRecord> voteRecordList,
            String rejectionOrDropReason
    ) {

        User user = User.builder()
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber)
                .password(password)
                .studentId(studentId)
                .admissionYear(admissionYear)
                .nickname(nickname)
                .major(major)
                .academicStatus(academicStatus)
                .currentCompletedSemester(currentCompletedSemester)
                .graduationYear(graduationYear)
                .graduationType(graduationType)
                .roles(roles)
                .userProfileImage(userProfileImage)
                .state(userState)
                .locker(locker)
                .circleMemberList(circleMemberList)
                .voteRecordList(voteRecordList)
                .rejectionOrDropReason(rejectionOrDropReason)
                .build();

        BaseEntityReflectionManager.setBaseEntityFields(
                user,
                id,
                createdAt,
                updatedAt
        );

        return user;
    }

}
