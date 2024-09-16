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

    @Query("SELECT uf " +
            "FROM UuidFile uf " +
            "WHERE NOT EXISTS (SELECT 1 FROM CalendarAttachImage cai WHERE cai.uuidFile = uf) " +
            "AND NOT EXISTS (SELECT 1 FROM CircleMainImage cmi WHERE cmi.uuidFile = uf) " +
            "AND NOT EXISTS (SELECT 1 FROM EventAttachImage eai WHERE eai.uuidFile = uf) " +
            "AND NOT EXISTS (SELECT 1 FROM PostAttachImage pai WHERE pai.uuidFile = uf) " +
            "AND NOT EXISTS (SELECT 1 FROM UserAcademicRecordApplicationAttachImage uaraai WHERE uaraai.uuidFile = uf) " +
            "AND NOT EXISTS (SELECT 1 FROM UserAdmissionAttachImage uaai WHERE uaai.uuidFile = uf)" +
            "AND NOT EXISTS (SELECT 1 FROM UserAdmissionLogAttachImage ualai WHERE ualai.uuidFile = uf) " +
            "AND NOT EXISTS (SELECT 1 FROM UserProfileImage upi WHERE upi.uuidFile = uf)")
    List<UuidFile> findUnusedUuidFileList();

}
