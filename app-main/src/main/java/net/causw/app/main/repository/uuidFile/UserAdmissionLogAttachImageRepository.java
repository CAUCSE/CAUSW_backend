package net.causw.app.main.repository.uuidFile;

import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserAdmissionLogAttachImage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAdmissionLogAttachImageRepository extends JpaRepository<UserAdmissionLogAttachImage, Long> {
}
