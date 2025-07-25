package net.causw.adapter.persistence.repository.userInfo;

import java.util.Optional;
import net.causw.adapter.persistence.userInfo.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

    Optional<UserInfo> findByUserId(String userId);
}