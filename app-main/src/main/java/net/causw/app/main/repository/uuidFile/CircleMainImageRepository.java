package net.causw.app.main.repository.uuidFile;

import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.CircleMainImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CircleMainImageRepository extends JpaRepository<CircleMainImage, Long> {
    boolean existsByUuidFile(UuidFile uuidFile);
}
