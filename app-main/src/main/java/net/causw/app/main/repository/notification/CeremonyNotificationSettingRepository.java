package net.causw.app.main.repository.notification;

import net.causw.app.main.domain.model.entity.notification.CeremonyNotificationSetting;
import net.causw.app.main.domain.model.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CeremonyNotificationSettingRepository extends JpaRepository<CeremonyNotificationSetting, String> {
	Optional<CeremonyNotificationSetting> findByUser(User user);

	@Query("SELECT DISTINCT c FROM CeremonyNotificationSetting c " +
		"WHERE :admissionYear MEMBER OF c.subscribedAdmissionYears OR c.isSetAll = true")
	List<CeremonyNotificationSetting> findByAdmissionYearOrSetAll(@Param("admissionYear") Integer admissionYear);
}
