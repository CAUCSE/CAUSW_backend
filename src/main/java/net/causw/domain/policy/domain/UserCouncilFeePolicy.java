package net.causw.domain.policy.domain;

import net.causw.adapter.persistence.semester.Semester;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import net.causw.domain.model.enums.semester.SemesterType;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;

public class UserCouncilFeePolicy {

  public static int determineRemainingAppliedSemestersWithUser(UserCouncilFee userCouncilFee) {
    return determineRemainingAppliedSemesters(
        userCouncilFee,
        userCouncilFee.getUser().getCurrentCompletedSemester());
  }

  public static int determineRemainingAppliedSemestersWithFakeUser(UserCouncilFee userCouncilFee) {
    return determineRemainingAppliedSemesters(
        userCouncilFee,
        userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester());
  }

  public static boolean isAppliedCurrentSemesterWithUser(UserCouncilFee userCouncilFee) {
    return isAppliedCurrentSemester(
        userCouncilFee,
        userCouncilFee.getUser().getCurrentCompletedSemester()
    );
  }

  public static boolean isAppliedCurrentSemesterWithFakeUser(UserCouncilFee userCouncilFee) {
    return isAppliedCurrentSemester(
        userCouncilFee,
        userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester()
    );
  }

  /**
   * 학생회비 적용 시작 학기를 결정합니다
   * <ul>
   * <li>현재 학기중이며 재학생인 경우 현재 학기부터 적용 시작</li>
   * <li>방학중이거나 휴학생인 경우 다음 학기부터 적용 시작</li>
   * </ul>
   * @param currentSemester
   * @param currentCompletedSemester
   * @param currentAcademicStatus
   * @return 학생회비 적용 시작 학기
   */
  public static int determineStartSemesterToApply(
      Semester currentSemester,
      Integer currentCompletedSemester, AcademicStatus currentAcademicStatus
  ) {
    SemesterType currentSemesterType = currentSemester.getSemesterType();
    boolean duringSemester = (currentSemesterType == SemesterType.FIRST) || (currentSemesterType == SemesterType.SECOND);

    if (currentAcademicStatus == AcademicStatus.ENROLLED && duringSemester) {
      return currentCompletedSemester;

    } else {
      return currentCompletedSemester + 1;
    }
  }

  /**
   * 잔여 학생회비 적용 학기를 결정합니다
   * <ul>
   *   <li>환불 받은 경우 잔여 학기 없음</li>
   *   <li>환불 받지 않은 경우 납부한 학기 수에서 학생회비 적용된 학기 수만큼 차감</li>
   * </ul>
   * @param userCouncilFee
   * @param currentCompletedSemester
   * @return 잔여 학생회비 적용 학기
   */
  private static int determineRemainingAppliedSemesters(UserCouncilFee userCouncilFee, Integer currentCompletedSemester) {
    if (userCouncilFee.getIsRefunded()) {
      return 0;
    }

    return Math.max(userCouncilFee.getNumOfPaidSemester() -
        (currentCompletedSemester - userCouncilFee.getPaidAt() + 1), 0);
  }

  /**
   * 현재 학기의 학생회비 적용 여부를 결정합니다
   * <ul>
   *   <li>현재 학기가 학생회비 적용 기간에 속하는지 확인</li>
   * </ul>
   * @param userCouncilFee
   * @param currentCompletedSemester
   * @return 현재 학기의 학생회비 적용 여부
   */
  private static boolean isAppliedCurrentSemester(UserCouncilFee userCouncilFee, Integer currentCompletedSemester) {
    int startOfAppliedSemester = userCouncilFee.getPaidAt();
    int endOfAppliedSemester = startOfAppliedSemester + userCouncilFee.getNumOfPaidSemester() - 1;

    return currentCompletedSemester >= startOfAppliedSemester &&
        currentCompletedSemester <= endOfAppliedSemester;
  }
}