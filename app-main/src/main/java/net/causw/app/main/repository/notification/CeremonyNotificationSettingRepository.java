package net.causw.app.main.repository.notification;

import net.causw.app.main.domain.model.entity.notification.CeremonyNotificationSetting;
import net.causw.app.main.domain.model.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CeremonyNotificationSettingRepository extends JpaRepository<CeremonyNotificationSetting, String> {
	Optional<CeremonyNotificationSetting> findByUser(User user);

	boolean existsByUser(User user);

	@Query("""
			SELECT DISTINCT c
			FROM CeremonyNotificationSetting c
			WHERE (:admissionYear MEMBER OF c.subscribedAdmissionYears OR c.isSetAll = true)
			AND (:#{#blockerUserIds.size()} = 0 OR c.user.id NOT IN :blockerUserIds)
			"""
	)
	List<CeremonyNotificationSetting> findByAdmissionYearOrSetAll(
        @Param("admissionYear") Integer admissionYear,
		@Param("blockerUserIds") Set<String> blockerUserIds
    );

	@Query("""
            SELECT DISTINCT c FROM CeremonyNotificationSetting c
            WHERE EXISTS (SELECT 1 FROM c.subscribedAdmissionYears s WHERE s IN :admissionYears) OR c.isSetAll = true
            AND (:#{#blockerUserIds.size()} = 0 OR c.user.id NOT IN :blockerUserIds)
            """
    )
	List<CeremonyNotificationSetting> findByAdmissionYearsIn(
        @Param("admissionYears") List<Integer> admissionYears,
        @Param("blockerUserIds") Set<String> blockerUserIds
    );
}
