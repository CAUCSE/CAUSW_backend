package net.causw.app.main.domain.asset.file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.UuidFile;

@Repository
public interface UuidFileRepository extends JpaRepository<UuidFile, String> {

	Optional<UuidFile> findByFileUrl(String fileUrl);

	List<UuidFile> findAllByIsUsed(Boolean isUsed);

}
