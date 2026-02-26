package net.causw.app.main.domain.user.account.repository.userInfo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.account.entity.userInfo.UserProject;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, String> {

	List<UserProject> findAllProjectByUserInfoId(String userInfoId);
}
