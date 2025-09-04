package net.causw.app.main.domain.policy;

import static net.causw.app.main.domain.policy.UserCouncilFeePolicy.getRemainingAppliedSemestersWithUser;
import static net.causw.app.main.domain.policy.UserCouncilFeePolicy.getStartSemesterToApply;
import static net.causw.app.main.domain.policy.UserCouncilFeePolicy.isAppliedCurrentSemesterWithUser;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import net.causw.app.main.domain.model.entity.semester.Semester;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userCouncilFee.UserCouncilFee;
import net.causw.app.main.domain.model.enums.semester.SemesterType;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.util.ObjectFixtures;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UserCouncilFeePolicyTest {

	@Nested
	class StartSemesterToApplyCasesTest {

		static final User user = ObjectFixtures.getUser();

		static Stream<Arguments> provideStartSemesterToApplyCases() {
			return Stream.of(
				Arguments.of(
					"현재 학기중이고 재학생인 경우 현재 학기부터 적용",
					SemesterType.FIRST, AcademicStatus.ENROLLED,
					user.getCurrentCompletedSemester()
				),

				Arguments.of(
					"현재 방학중인 경우 다음 학기부터 적용",
					SemesterType.SUMMER, AcademicStatus.ENROLLED,
					user.getCurrentCompletedSemester() + 1
				),

				Arguments.of(
					"현재 휴학생인 경우 다음 학기부터 적용",
					SemesterType.FIRST, AcademicStatus.LEAVE_OF_ABSENCE,
					user.getCurrentCompletedSemester() + 1
				)
			);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("provideStartSemesterToApplyCases")
		void test(String description, SemesterType semesterType, AcademicStatus academicStatus, int expectedSemester) {
			//given
			Semester currentSemester = Semester.of(2025, semesterType, null);
			user.setAcademicStatus(academicStatus);

			//when
			int actualSemester = getStartSemesterToApply(
				currentSemester, user.getCurrentCompletedSemester(), user.getAcademicStatus());

			//then
			assertThat(actualSemester).isEqualTo(expectedSemester);
		}
	}

	@Nested
	class RemainingAppliedSemestersTest {

		static Stream<Arguments> provideRemainingAppliedSemestersCases() {
			return Stream.of(
				Arguments.of(
					"학생회비 환불 받은 경우 잔여 학기 없음",
					createUserCouncilFee(1, 1, 8, true),
					0
				),

				Arguments.of(
					"다음 학기부터 학생회비 적용되는 경우 잔여 학기는 납부한 학기 수",
					createUserCouncilFee(2, 3, 6, false),
					6
				),

				Arguments.of(
					"이번 학기부터 학생회비 적용되는 경우 잔여 학기는 납부한 학기 수 - 1",
					createUserCouncilFee(3, 3, 6, false),
					5
				),

				Arguments.of(
					"신입생때 납부한 초과학기 재학생의 경우 잔여 학기 없음",
					createUserCouncilFee(9, 1, 8, false),
					0
				)
			);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("provideRemainingAppliedSemestersCases")
		void test(String description, UserCouncilFee userCouncilFee, int expectedSemester) {
			//given & when
			int actualSemester = getRemainingAppliedSemestersWithUser(userCouncilFee);

			//then
			assertThat(actualSemester).isEqualTo(expectedSemester);
		}
	}

	@Nested
	class IsAppliedCurrentSemesterTest {

		static Stream<Arguments> provideIsAppliedCurrentSemesterCases() {
			return Stream.of(
				Arguments.of(
					"현재 등록 완료 학기가 학생회비 적용 시작 학기 이전인 경우 학생회비 미적용",
					createUserCouncilFee(2, 3, 6, false),
					false
				),

				Arguments.of(
					"현재 등록 완료 학기가 학생회비 적용 시작 학기인 경우 학생회비 적용",
					createUserCouncilFee(3, 3, 6, false),
					true
				),

				Arguments.of(
					"잔여 학생회비 적용 학기가 없는 경우 학생회비 미적용",
					createUserCouncilFee(9, 1, 8, false),
					false
				)
			);
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("provideIsAppliedCurrentSemesterCases")
		void test(String description, UserCouncilFee userCouncilFee, boolean expectedResult) {
			//given & when
			boolean actualResult = isAppliedCurrentSemesterWithUser(userCouncilFee);

			//then
			assertThat(actualResult).isEqualTo(expectedResult);
		}
	}

	private static UserCouncilFee createUserCouncilFee(
		int currentCompletedSemester, int paidAt, int numOfPaidSemester, boolean isRefunded
	) {
		User user = ObjectFixtures.getUser();
		user.setCurrentCompletedSemester(currentCompletedSemester);

		return UserCouncilFee.of(
			true,
			user,
			null,
			paidAt,
			numOfPaidSemester,
			isRefunded,
			isRefunded ? 2025 : null
		);
	}
}