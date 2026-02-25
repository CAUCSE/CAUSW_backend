package net.causw.app.main.domain.user.account.repository.userInfo;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.account.entity.userInfo.UserInfo;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

	@NotNull
	Page<UserInfo> findAll(@NotNull Pageable pageable);

	Optional<UserInfo> findByUserId(String userId);
}
