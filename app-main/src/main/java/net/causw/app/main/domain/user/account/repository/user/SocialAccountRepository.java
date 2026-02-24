package net.causw.app.main.domain.user.account.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.account.entity.user.SocialAccount;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.SocialType;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, String> {

	@EntityGraph(attributePaths = {"user"})
	Optional<SocialAccount> findBySocialIdAndSocialType(String socialId, SocialType socialType);

	Boolean existsByUserAndSocialType(User user, SocialType socialType);
}
