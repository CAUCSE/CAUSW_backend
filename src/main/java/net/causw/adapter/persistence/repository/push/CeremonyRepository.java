package net.causw.adapter.persistence.repository.push;


import net.causw.adapter.persistence.push.Ceremony;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.ceremony.CeremonyState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CeremonyRepository extends JpaRepository<Ceremony, String> {
    List<Ceremony> findAllByUser(User user);
    Page<Ceremony> findByCeremonyState(CeremonyState ceremonyState, Pageable pageable);
}
