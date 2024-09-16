package net.causw.adapter.persistence.repository.uuidFile;

import net.causw.adapter.persistence.uuidFile.CalendarAttachImage;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface CalendarAttachImageRepository extends JpaRepository<CalendarAttachImage, Long> {
    boolean existsByUuidFile(UuidFile uuidFile);
}
