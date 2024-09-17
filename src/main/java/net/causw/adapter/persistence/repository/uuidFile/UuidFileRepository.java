package net.causw.adapter.persistence.repository.uuidFile;

import net.causw.adapter.persistence.uuidFile.UuidFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UuidFileRepository extends JpaRepository<UuidFile, String> {

    Optional<UuidFile> findByFileUrl(String fileUrl);

    List<UuidFile> findAllByIsUsed(Boolean isUsed);

}
