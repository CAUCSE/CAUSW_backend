package net.causw.domain.model.util;

import java.util.List;
import net.causw.adapter.persistence.semester.Semester;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.user.UserAdmission;
import net.causw.adapter.persistence.userCouncilFee.CouncilFeeFakeUser;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import net.causw.application.dto.user.UserCreateRequestDto;
import net.causw.domain.model.enums.semester.SemesterType;
import net.causw.domain.model.enums.user.GraduationType;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;

public class ObjectFixtures {

  public static User getUser() {
    UserCreateRequestDto userCreateRequestDto = new UserCreateRequestDto(
        "email@cau.ac.kr",
        "name",
        "password",
        "20002000",
        2000,
        "nickName",
        "major",
        "010-2000-2000"
    );

    User user = User.from(userCreateRequestDto, "password");
    user.setCurrentCompletedSemester(4);
    return user;
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
        GraduationType.FEBRUARY
    );
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
          0
      );
    } else {
      return UserCouncilFee.of(
          false,
          null,
          getCouncilFeeFakeUser(),
          1,
          8,
          false,
          0
      );
    }
  }

  public static UserAdmission getUserAdmission() {
    return UserAdmission.of(
        getUser(),
        List.of(),
        "description"
    );
  }

  public static Semester getSemester() {
    return Semester.of(
        2000,
        SemesterType.FIRST,
        getUser()
    );
  }
}
