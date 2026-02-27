package net.causw.app.main.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.asset.locker.entity.LockerName;
import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleDto;
import net.causw.app.main.domain.campus.semester.entity.Semester;
import net.causw.app.main.domain.campus.semester.enums.SemesterType;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.vote.entity.Vote;
import net.causw.app.main.domain.community.vote.entity.VoteOption;
import net.causw.app.main.domain.finance.usercouncilfee.entity.CouncilFeeFakeUser;
import net.causw.app.main.domain.finance.usercouncilfee.entity.UserCouncilFee;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.api.v1.dto.UserCreateRequestDto;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.GraduationType;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.dto.request.AdmissionCreateCommand;
import net.causw.global.constant.StaticValue;

public class ObjectFixtures {

	public static User getUser() {
		UserCreateRequestDto userCreateRequestDto = getUserCreateRequestDto();
		User user = User.from(userCreateRequestDto, "password");
		user.setCurrentCompletedSemester(4);
		return user;
	}

	public static User getCertifiedUser() {
		User user = getUser();
		user.setState(UserState.ACTIVE);
		user.setAcademicStatus(AcademicStatus.ENROLLED);
		user.setRoles(Set.of(Role.COMMON));

		return user;
	}

	public static User getUserWithId(String userId) {
		User user = getUser();
		ReflectionTestUtils.setField(user, "id", userId);
		return user;
	}

	public static User getCertifiedUserWithId(String userId) {
		User user = getCertifiedUser();
		ReflectionTestUtils.setField(user, "id", userId);

		return user;
	}

	public static User getRejectUserWithId(String userId) {
		User user = getUser();
		ReflectionTestUtils.setField(user, "id", userId);
		user.setState(UserState.REJECT);
		return user;
	}

	public static UserCreateRequestDto getUserCreateRequestDto() {
		return new UserCreateRequestDto(
			"email@cau.ac.kr",
			"name",
			"password123!",
			"20002000",
			2000,
			"nickName",
			"major",
			Department.SCHOOL_OF_SW,
			"010-2000-2000");
	}

	public static CouncilFeeFakeUser getCouncilFeeFakeUser() {
		return CouncilFeeFakeUser.of(
			"name",
			"20012001",
			"010-2001-2001",
			2001,
			"major",
			AcademicStatus.UNDETERMINED,
			4,
			2004,
			GraduationType.FEBRUARY);
	}

	public static UserCouncilFee getUserCouncilFee(boolean isJoinedService) {
		if (isJoinedService) {
			return UserCouncilFee.of(
				true,
				getUser(),
				null,
				1,
				8,
				false,
				0);
		} else {
			return UserCouncilFee.of(
				false,
				null,
				getCouncilFeeFakeUser(),
				1,
				8,
				false,
				0);
		}
	}

	/**
	 * v1 스타일 UserAdmission fixture.
	 */
	public static UserAdmission getUserAdmissionV1() {
		return UserAdmission.ofV1(
			getUser(),
			List.of(),
			"description");
	}

	/**
	 * v2 스타일 UserAdmission fixture.
	 */
	public static UserAdmission getUserAdmission(User user) {
		return UserAdmission.of(
			user,
			List.of(getUuidFile()),
			"재학증명서 첨부합니다",
			AcademicStatus.ENROLLED,
			"20231234",
			2023,
			Department.SCHOOL_OF_SW,
			null);
	}

	public static UserAdmission getUserAdmissionWithId(String admissionId, User user) {
		UserAdmission admission = getUserAdmission(user);
		ReflectionTestUtils.setField(admission, "id", admissionId);
		return admission;
	}

	// ── Admission 관련 요청/파일 fixture ──

	public static AdmissionCreateCommand getAdmissionCreateCommand() {
		return new AdmissionCreateCommand(
			"재학증명서 첨부합니다",
			AcademicStatus.ENROLLED,
			"20231234",
			2023,
			Department.SCHOOL_OF_SW,
			null);
	}

	public static List<MultipartFile> getMockAttachImages() {
		MockMultipartFile image = new MockMultipartFile(
			"attachImages",
			"test-image.png",
			"image/png",
			"image-content".getBytes());
		return List.of(image);
	}

	public static UuidFile getUuidFile() {
		UuidFile uuidFile = UuidFile.of(
			"test-uuid",
			"user-admission/test-image_test-uuid.png",
			"https://storage.example.com/user-admission/test-image_test-uuid.png",
			"test-image",
			"png",
			FilePath.USER_ADMISSION);
		ReflectionTestUtils.setField(uuidFile, "id", "uuid-file-1");
		return uuidFile;
	}

	public static Semester getSemester() {
		return Semester.of(
			2000,
			SemesterType.FIRST,
			getUser());
	}

	public static Board getBoard() {
		return Board.of(
			"boardName",
			"boardDescription",
			"category",
			true,
			null);
	}

	/**
	 * id가 설정된 Board fixture. 테스트에서 협력 객체 stub 시 일관된 id를 쓰기 위해 사용한다.
	 */
	public static Board getBoardWithId(String id) {
		Board board = getBoard();
		ReflectionTestUtils.setField(board, "id", id);
		return board;
	}

	public static Board getNoticeBoard(boolean isAlumni) {
		return Board.createNoticeBoard(
			"noticeBoardName",
			"boardDescription",
			List.of("ADMIN", "PRESIDENT", "VICE_PRESIDENT"),
			StaticValue.BOARD_NAME_APP_NOTICE,
			false,
			isAlumni,
			null);
	}

	/**
	 * id가 설정된 공지 게시판 fixture. 테스트에서 협력 객체 stub 시 일관된 id를 쓰기 위해 사용한다.
	 */
	public static Board getNoticeBoardWithId(boolean isAlumni, String id) {
		Board board = getNoticeBoard(isAlumni);
		ReflectionTestUtils.setField(board, "id", id);
		return board;
	}

	/**
	 * id가 설정된 v2 스타일 Board fixture. BoardService(v2) 등에서 사용한다.
	 */
	public static Board getBoardV2WithId(String id) {
		Board board = Board.createForV2("boardName", "boardDescription");
		ReflectionTestUtils.setField(board, "id", id);
		return board;
	}

	public static Post getPost(User user, Board board) {
		return Post.of(
			"title",
			"content",
			user,
			false,
			false,
			board,
			null,
			List.of());
	}

	public static Comment getComment(User user, Post post) {
		return Comment.of(
			"테스트 댓글",
			false,
			false,
			user,
			post);

	}

	public static ChildComment getChildComment(User user, Comment parentComment) {
		return ChildComment.of(
			"테스트 대댓글",
			false,
			false,
			user,
			parentComment);
	}

	public static List<VoteOption> getVoteOptions() {
		return List.of(VoteOption.of("option1"), VoteOption.of("option2"));
	}

	public static Vote getVote(List<VoteOption> voteOptions, Post post) {
		return Vote.of(
			"title",
			false,
			false,
			voteOptions,
			post);
	}

	public static Notification getNotification(User user) {
		return Notification.of(
			user,
			"최신 알림",
			"알림 내용",
			NoticeType.POST,
			"target-1",
			null);
	}

	// Schedule 관련 헬퍼 메서드
	public static Schedule getSchedule(User creator) {
		return Schedule.of(
			"중간고사 기간",
			ScheduleType.ACADEMIC,
			LocalDateTime.of(2026, 4, 15, 0, 0),
			LocalDateTime.of(2026, 4, 21, 23, 59),
			creator);
	}

	public static ScheduleDto getScheduleDto(User creator) {
		return ScheduleDto.builder()
			.id("schedule-id")
			.title("중간고사 기간")
			.type(ScheduleType.ACADEMIC)
			.start(LocalDateTime.of(2026, 4, 15, 0, 0))
			.end(LocalDateTime.of(2026, 4, 21, 23, 59))
			.creator(creator)
			.build();
	}

	// Locker 관련 헬퍼 메서드

	/**
	 * id가 설정되지 않은 기본 LockerLocation fixture.
	 */
	public static LockerLocation getLockerLocation(LockerName name) {
		return LockerLocation.of(name);
	}

	/**
	 * id가 설정된 LockerLocation fixture.
	 */
	public static LockerLocation getLockerLocationWithId(LockerName name, String id) {
		LockerLocation location = getLockerLocation(name);
		ReflectionTestUtils.setField(location, "id", id);
		return location;
	}

	/**
	 * id가 설정되지 않은 기본 Locker fixture.
	 */
	public static Locker getLocker(
		Long lockerNumber,
		Boolean isActive,
		User user,
		LockerLocation location,
		LocalDateTime expireDate) {
		return Locker.of(lockerNumber, isActive, user, location, expireDate);
	}

	/**
	 * id가 설정된 Locker fixture.
	 */
	public static Locker getLockerWithId(
		String id,
		Long lockerNumber,
		Boolean isActive,
		User user,
		LockerLocation location,
		LocalDateTime expireDate) {
		Locker locker = getLocker(lockerNumber, isActive, user, location, expireDate);
		ReflectionTestUtils.setField(locker, "id", id);
		return locker;
	}
}
