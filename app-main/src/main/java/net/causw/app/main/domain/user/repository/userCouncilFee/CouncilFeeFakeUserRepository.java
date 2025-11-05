package net.causw.app.main.domain.user.repository.userCouncilFee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.user.entity.userCouncilFee.CouncilFeeFakeUser;

@Repository
public interface CouncilFeeFakeUserRepository extends JpaRepository<CouncilFeeFakeUser, String> {
}
