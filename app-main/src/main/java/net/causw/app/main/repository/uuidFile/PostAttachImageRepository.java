package net.causw.app.main.repository.uuidFile;

import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.PostAttachImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostAttachImageRepository extends JpaRepository<PostAttachImage, Long> {
}
