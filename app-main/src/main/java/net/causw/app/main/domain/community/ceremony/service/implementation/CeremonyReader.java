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

	public Page<Ceremony> findAllOrderByStartedAtAsc(Pageable pageable) {
		return ceremonyRepository.findAllOrderByStartedAtAsc(pageable);
	}

	public Page<Ceremony> findOngoingByTypeOrderByStartedAtAsc(CeremonyType type, LocalDate nowDate, LocalTime nowTime,
		Pageable pageable) {
		return ceremonyRepository.findOngoingByTypeOrderByStartedAtAsc(type, nowDate, nowTime, pageable);
	}

	public Page<Ceremony> findUpcomingByTypeOrderByStartedAtAsc(CeremonyType type, LocalDate nowDate, LocalTime nowTime,
		LocalDate toDate, Pageable pageable) {
		return ceremonyRepository.findUpcomingByTypeOrderByStartedAtAsc(type, nowDate, nowTime, toDate, pageable);
	}

	public Page<Ceremony> findPastByTypeOrderByEndedAtAsc(CeremonyType type, LocalDate nowDate, LocalTime nowTime,
		LocalDate fromDate, Pageable pageable) {
		return ceremonyRepository.findPastByTypeOrderByEndedAtAsc(type, nowDate, nowTime, fromDate, pageable);
	}

	public Page<Ceremony> findAllOngoingOrderByStartedAtAsc(LocalDate nowDate, LocalTime nowTime, Pageable pageable) {
		return ceremonyRepository.findAllOngoingOrderByStartedAtAsc(nowDate, nowTime, pageable);
	}

	public Page<Ceremony> findAllUpcomingOrderByStartedAtAsc(LocalDate nowDate, LocalTime nowTime, LocalDate toDate,
		Pageable pageable) {
		return ceremonyRepository.findAllUpcomingOrderByStartedAtAsc(nowDate, nowTime, toDate, pageable);
	}

	public Page<Ceremony> findAllPastOrderByEndedAtAsc(LocalDate nowDate, LocalTime nowTime, LocalDate fromDate,
		Pageable pageable) {
		return ceremonyRepository.findAllPastOrderByEndedAtAsc(nowDate, nowTime, fromDate, pageable);
	}

	public Page<Ceremony> findMyByStateOrderByStartedAtAsc(String userId, CeremonyState state, Pageable pageable) {
		return ceremonyRepository.findMyByStateOrderByStartedAtAsc(userId, state, pageable);
	}
}
