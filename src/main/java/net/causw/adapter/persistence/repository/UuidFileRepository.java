package net.causw.adapter.persistence.repository;

import net.causw.adapter.persistence.UuidFile.UuidFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UuidFileRepository extends JpaRepository<UuidFile, String> {
    Optional<UuidFile> findByFileUrl(String fileUrl);
}
