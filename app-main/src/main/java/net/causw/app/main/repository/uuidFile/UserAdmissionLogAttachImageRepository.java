package net.causw.app.main.repository.uuidFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserAdmissionLogAttachImage;

@Repository
public interface UserAdmissionLogAttachImageRepository extends JpaRepository<UserAdmissionLogAttachImage, Long> {
}
