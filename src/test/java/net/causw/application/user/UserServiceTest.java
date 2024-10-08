package net.causw.application.user;

import jakarta.validation.Validator;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.repository.board.BoardRepository;
import net.causw.adapter.persistence.repository.circle.CircleMemberRepository;
import net.causw.adapter.persistence.repository.circle.CircleRepository;
import net.causw.adapter.persistence.repository.comment.ChildCommentRepository;
import net.causw.adapter.persistence.repository.comment.CommentRepository;
import net.causw.adapter.persistence.repository.locker.LockerLogRepository;
import net.causw.adapter.persistence.repository.locker.LockerRepository;
import net.causw.adapter.persistence.repository.post.FavoritePostRepository;
import net.causw.adapter.persistence.repository.post.LikePostRepository;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.repository.user.UserAdmissionLogRepository;
import net.causw.adapter.persistence.repository.user.UserAdmissionRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.repository.uuidFile.UserProfileImageRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.user.UserFindPasswordRequestDto;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.excel.UserExcelService;
import net.causw.application.pageable.PageableFactory;
import net.causw.application.util.ObjectFixtures;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.config.security.JwtTokenProvider;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.util.RedisUtils;
import net.causw.infrastructure.GoogleMailSender;
import net.causw.infrastructure.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;


@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    UuidFileService uuidFileService;

    @MockBean
    GoogleMailSender googleMailSender;

    @MockBean
    PasswordGenerator passwordGenerator;

    @MockBean
    PasswordEncoder passwordEncoder;

    @MockBean
    Validator validator;

    @MockBean
    UserRepository userRepository;

    @MockBean
    CircleRepository circleRepository;

    @MockBean
    CircleMemberRepository circleMemberRepository;

    @MockBean
    PostRepository postRepository;

    @MockBean
    PageableFactory pageableFactory;

    @MockBean
    CommentRepository commentRepository;

    @MockBean
    ChildCommentRepository childCommentRepository;

    @MockBean
    UserAdmissionRepository userAdmissionRepository;

    @MockBean
    RedisUtils redisUtils;

    @MockBean
    LockerRepository lockerRepository;

    @MockBean
    LockerLogRepository lockerLogRepository;

    @MockBean
    UserAdmissionLogRepository userAdmissionLogRepository;

    @MockBean
    BoardRepository boardRepository;

    @MockBean
    FavoritePostRepository favoritePostRepository;

    @MockBean
    LikePostRepository likePostRepository;

    @MockBean
    UserProfileImageRepository userProfileImageRepository;

    @MockBean
    UserExcelService userExcelService;

    User commonUser;

    User circleLeader;

    Circle circle1;

    Circle circle2;

    CircleMember circleMember1;

    CircleMember circleMember2;

    @BeforeEach
    void setUp() {
        commonUser = ObjectFixtures.getUser(
                AcademicStatus.ENROLLED,
                1,
                null,
                null,
                Set.of(Role.COMMON),
                UserState.ACTIVE,
                null,
                null,
                null
        );

        circleLeader = ObjectFixtures.getUser(
                AcademicStatus.ENROLLED,
                1,
                null,
                null,
                Set.of(Role.LEADER_CIRCLE),
                UserState.ACTIVE,
                null,
                null,
                null
        );

        Circle circle1 = ObjectFixtures.getCircle(circleLeader);

        Circle circle2 = ObjectFixtures.getCircle(circleLeader);

        CircleMember circleMember1 = ObjectFixtures.getCircleMember(circle1, commonUser);

        CircleMember circleMember2 = ObjectFixtures.getCircleMember(circle2, commonUser);
    }

    @DisplayName("비밀번호 찾기")
    @Test
    void findPassword() {
        // given
        UserFindPasswordRequestDto userFindPasswordRequestDto = new UserFindPasswordRequestDto(
                commonUser.getEmail(),
                commonUser.getStudentId(),
                commonUser.getStudentId(),
                commonUser.getPhoneNumber()
        );
        given(userRepository.findByEmailAndNameAndStudentIdAndPhoneNumber(
                userFindPasswordRequestDto.getEmail(),
                userFindPasswordRequestDto.getName(),
                userFindPasswordRequestDto.getStudentId(),
                userFindPasswordRequestDto.getPhoneNumber()
        )).willReturn(Optional.ofNullable(commonUser));

        // when
        UserResponseDto userResponseDto = userService.findPassword(userFindPasswordRequestDto);

        // then
        assertEquals(commonUser.getId(), userResponseDto.getId());
        assertEquals(commonUser.getEmail(), userResponseDto.getEmail());
        assertEquals(commonUser.getName(), userResponseDto.getName());
        assertEquals(commonUser.getStudentId(), userResponseDto.getStudentId());
        assertEquals(commonUser.getAdmissionYear(), userResponseDto.getAdmissionYear());
        assertEquals(commonUser.getRoles(), userResponseDto.getRoles());
        assertEquals(commonUser.getUserProfileImage().getUuidFile().getFileUrl(), userResponseDto.getProfileImageUrl());
        assertEquals(commonUser.getState(), userResponseDto.getState());
        assertEquals(new ArrayList<>(), userResponseDto.getCircleIdIfLeader());
        assertEquals(new ArrayList<>(), userResponseDto.getCircleNameIfLeader());
        assertEquals(commonUser.getNickname(), userResponseDto.getNickname());
        assertEquals(commonUser.getMajor(), userResponseDto.getMajor());
        assertEquals(commonUser.getAcademicStatus(), userResponseDto.getAcademicStatus());
        assertEquals(commonUser.getCurrentCompletedSemester(), userResponseDto.getCurrentCompletedSemester());
        assertEquals(commonUser.getGraduationYear(), userResponseDto.getGraduationYear());
        assertEquals(commonUser.getGraduationType(), userResponseDto.getGraduationType());
        assertEquals(commonUser.getPhoneNumber(), userResponseDto.getPhoneNumber());
        assertEquals(commonUser.getRejectionOrDropReason(), userResponseDto.getRejectionOrDropReason());
        assertEquals(commonUser.getCreatedAt(), userResponseDto.getCreatedAt());
        assertEquals(commonUser.getUpdatedAt(), userResponseDto.getUpdatedAt());
    }

}
