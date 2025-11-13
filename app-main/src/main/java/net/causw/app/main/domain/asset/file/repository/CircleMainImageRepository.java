package net.causw.app.main.domain.asset.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.CircleMainImage;

@Repository
public interface CircleMainImageRepository extends JpaRepository<CircleMainImage, Long> {
	boolean existsByUuidFile(UuidFile uuidFile);
}
