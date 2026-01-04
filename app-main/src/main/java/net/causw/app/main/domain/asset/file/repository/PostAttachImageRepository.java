package net.causw.app.main.domain.asset.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.joinEntity.PostAttachImage;

@Repository
public interface PostAttachImageRepository extends JpaRepository<PostAttachImage, Long> {}
