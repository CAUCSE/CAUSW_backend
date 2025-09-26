package net.causw.app.main.repository.userInfo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.userInfo.UserInfo;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

	Optional<UserInfo> findByUserId(String userId);
}
