package net.causw.app.main.repository.uuidFile;

import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.ChatRoomProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomProfileImageRepository extends JpaRepository<ChatRoomProfileImage, Long> {
}
