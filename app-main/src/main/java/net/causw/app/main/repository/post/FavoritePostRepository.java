package net.causw.app.main.repository.post;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.causw.app.main.domain.model.entity.post.FavoritePost;

public interface FavoritePostRepository extends JpaRepository<FavoritePost, String> {
	Boolean existsByPostIdAndUserId(String postId, String userId);

	Long countByPostId(String postId);

	void deleteFavoriteByPostIdAndUserId(String postId, String userId);

	@Query("""
		    SELECT fp
		    FROM FavoritePost fp
		    JOIN fp.post p
		    WHERE fp.user.id = :userId
		    AND (:#{#blockedUserIds.size()} = 0 OR p.writer.id NOT IN :blockedUserIds)
		    ORDER BY p.createdAt DESC
		"""
	)
	Page<FavoritePost> findByUserId(@Param("userId") String userId, @Param("blockedUserIds") Set<String> blockedUserIds,
		Pageable pageable);
}
