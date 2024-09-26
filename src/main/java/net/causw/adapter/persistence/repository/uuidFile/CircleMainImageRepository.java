package net.causw.adapter.persistence.repository.uuidFile;

import net.causw.adapter.persistence.uuidFile.joinEntity.CircleMainImage;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CircleMainImageRepository extends JpaRepository<CircleMainImage, Long> {
    boolean existsByUuidFile(UuidFile uuidFile);
}
