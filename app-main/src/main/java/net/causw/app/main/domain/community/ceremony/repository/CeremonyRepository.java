package net.causw.app.main.domain.community.ceremony.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.user.account.entity.user.User;

public interface CeremonyRepository extends JpaRepository<Ceremony, String> {
	Page<Ceremony> findAllByUserAndCeremonyStateOrderByCreatedAtDesc(User user, CeremonyState ceremonyState,
		Pageable pageable);

	Page<Ceremony> findByCeremonyStateOrderByCreatedAtDesc(CeremonyState ceremonyState, Pageable pageable);

	Optional<Ceremony> findByIdAndUser(String id, User user);

}
