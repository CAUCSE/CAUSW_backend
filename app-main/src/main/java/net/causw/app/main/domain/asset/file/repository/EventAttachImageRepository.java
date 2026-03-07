package net.causw.app.main.domain.asset.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.joinEntity.EventAttachImage;

@Repository
public interface EventAttachImageRepository extends JpaRepository<EventAttachImage, Long> {}
