package net.causw.app.main.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.test.util.ReflectionTestUtils;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.service.v2.dto.ScheduleDto;
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
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.api.v1.dto.UserCreateRequestDto;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.GraduationType;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
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

	public static User getCertifiedUserWithId(String userId) {
		User user = getCertifiedUser();
		ReflectionTestUtils.setField(user, "id", userId);

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

	public static UserAdmission getUserAdmission() {
		return UserAdmission.of(
			getUser(),
			List.of(),
			"description");
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
}
