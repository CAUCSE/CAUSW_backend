package net.causw.app.main.domain.asset.file.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.UuidFile;

@Repository
public interface UuidFileRepository extends JpaRepository<UuidFile, String> {

	Optional<UuidFile> findByFileUrl(String fileUrl);

	Optional<UuidFile> findByUuid(String uuid);

	List<UuidFile> findAllByUuidIn(List<String> uuids);

	Page<UuidFile> findAllByIsUploadedFalseAndCreatedAtBefore(LocalDateTime cutoff, Pageable pageable);

	@Modifying
	@Query("UPDATE UuidFile f SET f.isUploaded = true, f.isUsed = true WHERE f.uuid IN :uuids AND f.isUploaded = false")
	int confirmByUuids(@Param("uuids") List<String> uuids);

}
