package net.causw.app.main.domain.user.account.repository.userInfo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

	Optional<UserInfo> findByUserId(String userId);

	List<UserInfo> findAllByUser_IdIn(List<String> userIds);
}
