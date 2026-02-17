package net.causw.app.main.domain.community.ceremony.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;

public interface CeremonyRepository extends JpaRepository<Ceremony, String> {

	Page<Ceremony> findByUser_IdAndCeremonyStateOrderByStartDateDescStartTimeDesc(String userId,
		CeremonyState ceremonyState, Pageable pageable);
}
