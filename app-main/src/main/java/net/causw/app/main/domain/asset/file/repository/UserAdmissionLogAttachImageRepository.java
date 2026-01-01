package net.causw.app.main.domain.asset.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserAdmissionLogAttachImage;

@Repository
public interface UserAdmissionLogAttachImageRepository extends JpaRepository<UserAdmissionLogAttachImage, Long> {}
