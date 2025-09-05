package net.causw.app.main.repository.uuidFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.CircleMainImage;

@Repository
public interface CircleMainImageRepository extends JpaRepository<CircleMainImage, Long> {
	boolean existsByUuidFile(UuidFile uuidFile);
}
