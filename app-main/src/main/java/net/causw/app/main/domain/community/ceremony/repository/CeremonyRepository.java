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

	@Query("SELECT c " +
		"FROM Ceremony c " +
		"WHERE (c.ceremonyState = 'ACCEPT') " +
		"ORDER BY c.startDate, c.startTime ASC")
	Page<Ceremony> findAllOrderByStartedAtAsc(Pageable pageable);

	@Query("SELECT c " +
		"FROM Ceremony c " +
		"WHERE (c.ceremonyState = 'ACCEPT' AND c.ceremonyType = :type)" +
		"AND ((c.startDate < :nowDate AND :nowDate < c.endDate) " +
		"OR ((c.startDate = :nowDate AND c.endDate = :nowDate) AND (c.startTime <= :nowTime AND :nowTime <= c.endTime)) "
		+
		"OR ((c.startDate = :nowDate AND :nowDate < c.endDate) AND (c.startTime <= :nowTime)) " +
		"OR ((c.startDate < :nowDate AND c.endDate = :nowDate) AND (:nowTime <= c.endTime)) " +
		"OR ((c.startDate = :nowDate AND c.endDate = :nowDate) AND c.startTime IS NULL)) " +
		"ORDER BY c.startTime ASC")
	Page<Ceremony> findOngoingByTypeOrderByStartedAtAsc(
		@Param("type") CeremonyType type, @Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime,
		Pageable pageable);

	@Query("SELECT c " +
		"FROM Ceremony c " +
		"WHERE (c.ceremonyState = 'ACCEPT' AND c.ceremonyType = :type) " +
		"AND ((:nowDate < c.startDate AND c.startDate <= :toDate) " +
		"OR (c.startDate = :nowDate AND (c.startTime IS NOT NULL AND :nowTime < c.startTime))) " +
		"ORDER BY c.startDate, c.startTime ASC")
	Page<Ceremony> findUpcomingByTypeOrderByStartedAtAsc(
		@Param("type") CeremonyType type, @Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime,
		@Param("toDate") LocalDate toDate, Pageable pageable);

	@Query("SELECT c " +
		"FROM Ceremony c " +
		"WHERE (c.ceremonyState = 'ACCEPT' AND c.ceremonyType = :type) " +
		"AND ((:fromDate <= c.endDate AND c.endDate < :nowDate) " +
		"OR (c.endDate = :nowDate AND (c.endTime IS NOT NULL AND c.endTime < :nowTime))) " +
		"ORDER BY c.endDate, c.endTime ASC")
	Page<Ceremony> findPastByTypeOrderByEndedAtAsc(
		@Param("type") CeremonyType type, @Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime,
		@Param("fromDate") LocalDate fromDate, Pageable pageable);

	@Query("SELECT c " +
		"FROM Ceremony c " +
		"WHERE c.ceremonyState = 'ACCEPT' " +
		"AND ((c.startDate < :nowDate AND :nowDate < c.endDate) " +
		"OR ((c.startDate = :nowDate AND c.endDate = :nowDate) AND (c.startTime <= :nowTime AND :nowTime <= c.endTime)) "
		+
		"OR ((c.startDate = :nowDate AND :nowDate < c.endDate) AND (c.startTime <= :nowTime)) " +
		"OR ((c.startDate < :nowDate AND c.endDate = :nowDate) AND (:nowTime <= c.endTime)) " +
		"OR ((c.startDate = :nowDate AND c.endDate = :nowDate) AND c.startTime IS NULL)) " +
		"ORDER BY c.startTime ASC")
	Page<Ceremony> findAllOngoingOrderByStartedAtAsc(
		@Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime, Pageable pageable);

	@Query("SELECT c " +
		"FROM Ceremony c " +
		"WHERE c.ceremonyState = 'ACCEPT' " +
		"AND ((:nowDate < c.startDate AND c.startDate <= :toDate) " +
		"OR (c.startDate = :nowDate AND (c.startTime IS NOT NULL AND :nowTime < c.startTime))) " +
		"ORDER BY c.startDate, c.startTime ASC")
	Page<Ceremony> findAllUpcomingOrderByStartedAtAsc(
		@Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime, @Param("toDate") LocalDate toDate,
		Pageable pageable);

	@Query("SELECT c " +
		"FROM Ceremony c " +
		"WHERE c.ceremonyState = 'ACCEPT' " +
		"AND ((:fromDate <= c.endDate AND c.endDate < :nowDate) " +
		"OR (c.endDate = :nowDate AND (c.endTime IS NOT NULL AND c.endTime < :nowTime))) " +
		"ORDER BY c.endDate, c.endTime ASC")
	Page<Ceremony> findAllPastOrderByEndedAtAsc(
		@Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime, @Param("fromDate") LocalDate fromDate,
		Pageable pageable);

	@Query("SELECT c " +
		"FROM Ceremony c " +
		"WHERE (c.user.id = :userId AND (:state IS NULL OR c.ceremonyState = :state)) " +
		"ORDER BY c.startDate, c.startTime ASC")
	Page<Ceremony> findMyByStateOrderByStartedAtAsc(@Param("userId") String userId, @Param("state") CeremonyState state,
		Pageable pageable);
}
