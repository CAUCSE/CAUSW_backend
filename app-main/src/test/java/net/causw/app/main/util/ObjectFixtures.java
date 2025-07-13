package net.causw.app.main.util;

import java.util.List;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.semester.Semester;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.user.UserAdmission;
import net.causw.app.main.domain.model.entity.userCouncilFee.CouncilFeeFakeUser;
import net.causw.app.main.domain.model.entity.userCouncilFee.UserCouncilFee;
import net.causw.app.main.domain.model.entity.vote.Vote;
import net.causw.app.main.domain.model.entity.vote.VoteOption;
import net.causw.app.main.dto.user.UserCreateRequestDto;
import net.causw.app.main.domain.model.enums.semester.SemesterType;
import net.causw.app.main.domain.model.enums.user.GraduationType;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

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

  public static Board getBoard() {
    return Board.of(
        "boardName",
        "boardDescription",
        List.of(
            "ADMIN", "PRESIDENT", "VICE_PRESIDENT",
            "COUNCIL", "LEADER_1", "LEADER_2", "LEADER_3", "LEADER_4",
            "LEADER_CIRCLE", "LEADER_ALUMNI", "COMMON", "PROFESSOR"),
        "category",
        true,
        null
    );
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
        List.of()
    );
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
        post
    );
  }
}
