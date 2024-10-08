package net.causw.adapter.persistence.repository.uuidFile;

import net.causw.adapter.persistence.uuidFile.joinEntity.CalendarAttachImage;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarAttachImageRepository extends JpaRepository<CalendarAttachImage, Long> {

    @NotNull Page<CalendarAttachImage> findAll(@NotNull Pageable pageable);

}
