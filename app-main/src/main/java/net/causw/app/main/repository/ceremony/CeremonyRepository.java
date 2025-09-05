package net.causw.app.main.repository.ceremony;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.model.entity.ceremony.Ceremony;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.ceremony.CeremonyState;

public interface CeremonyRepository extends JpaRepository<Ceremony, String> {
	Page<Ceremony> findAllByUserAndCeremonyStateOrderByCreatedAtDesc(User user, CeremonyState ceremonyState,
		Pageable pageable);

	Page<Ceremony> findByCeremonyStateOrderByCreatedAtDesc(CeremonyState ceremonyState, Pageable pageable);

	Optional<Ceremony> findByIdAndUser(String id, User user);

}
