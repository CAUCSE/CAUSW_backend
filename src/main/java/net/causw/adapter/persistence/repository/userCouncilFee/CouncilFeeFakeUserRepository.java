package net.causw.adapter.persistence.repository.userCouncilFee;

import net.causw.adapter.persistence.userCouncilFee.CouncilFeeFakeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouncilFeeFakeUserRepository extends JpaRepository<CouncilFeeFakeUser, String> {
}
