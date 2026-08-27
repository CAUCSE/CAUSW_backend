package net.causw.app.main.domain.community.systemnotice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.systemnotice.entity.UserSystemNoticeRead;

@Repository
public interface UserSystemNoticeReadRepository extends JpaRepository<UserSystemNoticeRead, String> {

	Optional<UserSystemNoticeRead> findByUserId(String userId);

	@Modifying
	@Query(value = """
		INSERT INTO tb_user_system_notice_read (user_id, last_read_post_id, created_at, updated_at)
		VALUES (:userId, :postId, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
		ON DUPLICATE KEY UPDATE
			last_read_post_id = VALUES(last_read_post_id),
			updated_at = CURRENT_TIMESTAMP(6)
		""", nativeQuery = true)
	void upsertLastReadPost(@Param("userId") String userId, @Param("postId") String postId);
}
