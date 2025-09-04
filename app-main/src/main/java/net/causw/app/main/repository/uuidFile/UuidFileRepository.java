package net.causw.app.main.repository.uuidFile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;

@Repository
public interface UuidFileRepository extends JpaRepository<UuidFile, String> {

	Optional<UuidFile> findByFileUrl(String fileUrl);

	List<UuidFile> findAllByIsUsed(Boolean isUsed);

}
