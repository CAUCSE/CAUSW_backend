package net.causw.adapter.persistence.repository.push;


import net.causw.adapter.persistence.ceremony.Ceremony;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.ceremony.CeremonyState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CeremonyRepository extends JpaRepository<Ceremony, String> {
    Page<Ceremony> findAllByUserAndCeremonyState(User user, CeremonyState ceremonyState, Pageable pageable);
    Page<Ceremony> findByCeremonyState(CeremonyState ceremonyState, Pageable pageable);

    Optional<Ceremony> findByIdAndUser(String id, User user);

}
