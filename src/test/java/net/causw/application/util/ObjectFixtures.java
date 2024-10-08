package net.causw.application.util;

import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.adapter.persistence.uuidFile.joinEntity.CircleMainImage;
import net.causw.adapter.persistence.uuidFile.joinEntity.UserProfileImage;
import net.causw.adapter.persistence.vote.VoteRecord;
import net.causw.domain.model.enums.circle.CircleMemberStatus;
import net.causw.domain.model.enums.user.GraduationType;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.enums.uuidFile.FilePath;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 테스트에서 사용할 객체를 생성하는 메서드를 정의하는 Fixture 클래스입니다.
 * 중요하지 않은 값들은 여기서 고정값으로 정의하고, 테스트에서 필요한 값만 변경해서 사용합니다.
 * 매개변수를 다르게 하여 값이 일부 다른 객체를 생성하기 위해서 별도의 메서드를 추가로 정의할 수도 있습니다.
 */

public class ObjectFixtures {

    // user
    public static User getUser(
            AcademicStatus academicStatus,
            Integer currentCompletedSemester,
            Integer graduationYear,
            GraduationType graduationType,
            Set<Role> roles,
            UserState userState,
            Locker locker,
            List<CircleMember> circleMemberList,
            List<VoteRecord> voteRecordList
    ) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        String encodedPassword = passwordEncoder.encode("password123*");

        UuidFile uuidFile = UuidFileObjectBuilder.buildUuidFile(
                "uuidFileMockId",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "uuidMock",
                "fileKeyMock",
                "rawFileNameMock",
                "extensionMock",
                FilePath.USER_PROFILE,
                true
        );

        UserProfileImage userProfileImage = UserProfileImageObjectBuilder.buildUserProfileImageReduced(
                "userProfileImageMockId",
                LocalDateTime.now(),
                LocalDateTime.now(),
                uuidFile
        );

        User user = UserObjectBuilder.buildUser(
                "userMockId",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "mock@gmail.com",
                "정상제",
                "01012345678",
                encodedPassword,
                "20191111",
                2019,
                "닉네임 mock",
                "소프트웨어학부",
                academicStatus,
                currentCompletedSemester,
                graduationYear,
                graduationType,
                roles,
                userProfileImage,
                userState,
                locker,
                circleMemberList,
                voteRecordList,
                "탈퇴 사유 mock"
        );

        UserProfileImageObjectBuilder.setUserProfileImageUser(user.getUserProfileImage(), user);

        return user;
    }

    public static Circle getCircle(
            User leader
    ) {
        CircleMainImage circleMainImage = CircleMainImageObjectBuilder.buildCircleMainImageReduced(
                "circleMainImageMockId",
                LocalDateTime.now(),
                LocalDateTime.now(),
                UuidFileObjectBuilder.buildUuidFile(
                        "uuidFileMockId",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        "uuidMock",
                        "fileKeyMock",
                        "rawFileNameMock",
                        "extensionMock",
                        FilePath.CIRCLE_PROFILE,
                        true
                )
        );

        Circle circle = CircleObjectBuilder.buildCircle(
                "circleMockId",
                LocalDateTime.now(),
                LocalDateTime.now(),
                "동아리 이름 mock",
                circleMainImage,
                "동아리 설명 mock",
                false,
                10000,
                10,
                LocalDateTime.now(),
                true,
                leader
        );

        CircleMainImageObjectBuilder.setCircleMainImageCircle(circle.getCircleMainImage(), circle);

        return circle;
    }

    public static CircleMember getCircleMember(
            Circle circle,
            User user
    ) {
        return CircleMemberObjectFixtures.buildCircleMember(
                "circleMemberMockId",
                LocalDateTime.now(),
                LocalDateTime.now(),
                CircleMemberStatus.MEMBER,
                circle,
                user,
                null,
                null
        );
    }

}
