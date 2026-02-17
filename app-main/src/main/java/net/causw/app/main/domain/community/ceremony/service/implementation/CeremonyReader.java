package net.causw.app.main.domain.community.ceremony.service.implementation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;
import net.causw.app.main.domain.community.ceremony.repository.CeremonyRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CeremonyReader {

	private final CeremonyRepository ceremonyRepository;

	public Optional<Ceremony> findById(String ceremonyId) {
		return ceremonyRepository.findById(ceremonyId);
	}

	public Page<Ceremony> findAllOngoingOrderByStartedAtDesc(LocalDate nowDate, LocalTime nowTime, Pageable pageable) {
		return ceremonyRepository.findAllOngoingOrderByStartedAtDesc(nowDate, nowTime, pageable);
	}

	public Page<Ceremony> findOngoingByTypeOrderByStartedAtDesc(CeremonyType type, LocalDate nowDate, LocalTime nowTime,
		Pageable pageable) {
		return ceremonyRepository.findOngoingByTypeOrderByStartedAtDesc(type, nowDate, nowTime, pageable);
	}

	public Page<Ceremony> findAllUpcomingOrderByStartedAtAsc(LocalDate nowDate, LocalTime nowTime, Pageable pageable) {
		return ceremonyRepository.findAllUpcomingOrderByStartedAtAsc(nowDate, nowTime, pageable);
	}

	public Page<Ceremony> findUpcomingByTypeOrderByStartedAtAsc(CeremonyType type, LocalDate nowDate, LocalTime nowTime,
		Pageable pageable) {
		return ceremonyRepository.findUpcomingByTypeOrderByStartedAtAsc(type, nowDate, nowTime, pageable);
	}

	public Page<Ceremony> findAllPastOrderByStartedAtDesc(LocalDate nowDate, LocalTime nowTime, Pageable pageable) {
		return ceremonyRepository.findAllPastOrderByStartedAtDesc(nowDate, nowTime, pageable);
	}

	public Page<Ceremony> findPastByTypeOrderByStartedAtDesc(CeremonyType type, LocalDate nowDate, LocalTime nowTime,
		Pageable pageable) {
		return ceremonyRepository.findPastByTypeOrderByStartedAtDesc(type, nowDate, nowTime, pageable);
	}

	public Page<Ceremony> findByUserIdAndCeremonyStateOrderByStartedAtDesc(String userId, CeremonyState state,
		Pageable pageable) {
		return ceremonyRepository.findByUser_IdAndCeremonyStateOrderByStartDateDescStartTimeDesc(userId, state,
			pageable);
	}
}
