package net.causw.adapter.persistence.repository.notification;

import net.causw.adapter.persistence.notification.CeremonyNotificationSetting;
import net.causw.adapter.persistence.user.User;
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
