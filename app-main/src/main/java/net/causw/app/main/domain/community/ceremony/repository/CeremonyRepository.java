package net.causw.app.main.domain.community.ceremony.repository;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;

public interface CeremonyRepository extends JpaRepository<Ceremony, String> {

	@Query("""
		SELECT c FROM Ceremony c WHERE c.ceremonyState = 'ACCEPT'

		AND c.startDate <= :nowDate
		AND ((c.startTime IS NULL AND (
			   (c.endDate IS NULL AND :nowDate = c.startDate)
			OR (c.endDate IS NOT NULL AND :nowDate <= c.endDate)
		))
		OR (c.startTime IS NOT NULL AND (
			     (c.startDate < :nowDate AND :nowDate < c.endDate)
			OR ( (c.startDate = :nowDate AND :nowDate = c.endDate) AND (c.startTime <= :nowTime AND :nowTime <= c.endTime) )
			OR ( (c.startDate = :nowDate AND :nowDate < c.endDate) AND (c.startTime <= :nowTime) )
			OR ( (c.startDate < :nowDate AND :nowDate = c.endDate) AND (:nowTime <= c.endTime) )
			))
		)
		ORDER BY c.startDate, c.startTime DESC
		""")
	Page<Ceremony> findAllOngoingOrderByStartedAtDesc(
		@Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime, Pageable pageable);

	@Query("""
		SELECT c FROM Ceremony c WHERE c.ceremonyState = 'ACCEPT'
		AND c.ceremonyType = :type

		AND c.startDate <= :nowDate
		AND ((c.startTime IS NULL AND (
			   (c.endDate IS NULL AND :nowDate = c.startDate)
			OR (c.endDate IS NOT NULL AND :nowDate <= c.endDate)
		))
		OR (c.startTime IS NOT NULL AND (
			     (c.startDate < :nowDate AND :nowDate < c.endDate)
			OR ( (c.startDate = :nowDate AND :nowDate = c.endDate) AND (c.startTime <= :nowTime AND :nowTime <= c.endTime) )
			OR ( (c.startDate = :nowDate AND :nowDate < c.endDate) AND (c.startTime <= :nowTime) )
			OR ( (c.startDate < :nowDate AND :nowDate = c.endDate) AND (:nowTime <= c.endTime) )
			))
		)
		ORDER BY c.startDate, c.startTime DESC
		""")
	Page<Ceremony> findOngoingByTypeOrderByStartedAtDesc(
		@Param("type") CeremonyType type, @Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime,
		Pageable pageable);

	@Query("""
		SELECT c FROM Ceremony c WHERE c.ceremonyState = 'ACCEPT'

		AND (:nowDate < c.startDate
		OR (:nowDate = c.startDate AND c.startTime IS NOT NULL AND :nowTime < c.startTime)
		)
		ORDER BY c.startDate, c.startTime ASC
		""")
	Page<Ceremony> findAllUpcomingOrderByStartedAtAsc(
		@Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime, Pageable pageable);

	@Query("""
		SELECT c FROM Ceremony c WHERE c.ceremonyState = 'ACCEPT'
		AND c.ceremonyType = :type

		AND (:nowDate < c.startDate
		OR (:nowDate = c.startDate AND c.startTime IS NOT NULL AND :nowTime < c.startTime)
		)
		ORDER BY c.startDate, c.startTime ASC
		""")
	Page<Ceremony> findUpcomingByTypeOrderByStartedAtAsc(
		@Param("type") CeremonyType type, @Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime,
		Pageable pageable);

	@Query("""
		SELECT c FROM Ceremony c WHERE c.ceremonyState = 'ACCEPT'

		AND (c.startTime IS NULL AND (
				c.endDate IS NULL AND c.startDate < :nowDate
			OR (c.endDate IS NOT NULL AND c.endDate < :nowDate)
		)
		OR (c.startTime IS NOT NULL AND (
			c.endDate < :nowDate OR (c.endDate = :nowDate AND c.endTime < :nowTime)
		)))
		ORDER BY c.startDate, c.startTime DESC
		""")
	Page<Ceremony> findAllPastOrderByStartedAtDesc(
		@Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime, Pageable pageable);

	@Query("""
		SELECT c FROM Ceremony c WHERE c.ceremonyState = 'ACCEPT'
		AND c.ceremonyType = :type

		AND (c.startTime IS NULL AND (
				c.endDate IS NULL AND c.startDate < :nowDate
			OR (c.endDate IS NOT NULL AND c.endDate < :nowDate)
		)
		OR (c.startTime IS NOT NULL AND (
			c.endDate < :nowDate OR (c.endDate = :nowDate AND c.endTime < :nowTime)
		)))
		ORDER BY c.startDate, c.startTime DESC
		""")
	Page<Ceremony> findPastByTypeOrderByStartedAtDesc(
		@Param("type") CeremonyType type, @Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime,
		Pageable pageable);

	Page<Ceremony> findByUser_IdAndCeremonyStateOrderByStartDateDescStartTimeDesc(String userId,
		CeremonyState ceremonyState, Pageable pageable);
}
