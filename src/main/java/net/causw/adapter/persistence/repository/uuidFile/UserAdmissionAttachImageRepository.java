package net.causw.adapter.persistence.repository.uuidFile;

import net.causw.adapter.persistence.uuidFile.joinEntity.UserAdmissionAttachImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAdmissionAttachImageRepository extends JpaRepository<UserAdmissionAttachImage, Long> {
}
