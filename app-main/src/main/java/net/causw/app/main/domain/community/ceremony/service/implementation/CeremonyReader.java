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
import net.causw.app.main.domain.community.ceremony.repository.CeremonyRepository;
import net.causw.app.main.domain.community.ceremony.repository.query.CeremonyQueryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CeremonyReader {

	private final CeremonyRepository ceremonyRepository;
	private final CeremonyQueryRepository ceremonyQueryRepository;

	public Optional<Ceremony> findById(String ceremonyId) {
		return ceremonyRepository.findById(ceremonyId);
	}

	public Page<Ceremony> findOngoingOrderByStartedAtDesc(String type, LocalDate nowDate, LocalTime nowTime,
		Pageable pageable) {
		return ceremonyQueryRepository.findOngoingOrderByStartedAtDesc(type, nowDate, nowTime, pageable);
	}

	public Page<Ceremony> findUpcomingOrderByStartedAtAsc(String type, LocalDate nowDate, LocalTime nowTime,
		Pageable pageable) {
		return ceremonyQueryRepository.findUpcomingOrderByStartedAtAsc(type, nowDate, nowTime, pageable);
	}

	public Page<Ceremony> findPastOrderByStartedAtDesc(String type, LocalDate nowDate, LocalTime nowTime,
		Pageable pageable) {
		return ceremonyQueryRepository.findPastOrderByStartedAtDesc(type, nowDate, nowTime, pageable);
	}

	public Page<Ceremony> findByUserIdAndCeremonyStateOrderByStartedAtDesc(String userId, CeremonyState state,
		Pageable pageable) {
		return ceremonyRepository.findByUser_IdAndCeremonyStateOrderByStartDateDescStartTimeDesc(userId, state,
			pageable);
	}

	public Page<Ceremony> findAllForAdmin(LocalDate fromDate, LocalDate toDate, CeremonyState state,
		Pageable pageable) {
		return ceremonyQueryRepository.findAllForAdmin(fromDate, toDate, state, pageable);
	}
}
