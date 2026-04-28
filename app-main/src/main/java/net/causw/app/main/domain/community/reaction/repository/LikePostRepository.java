package net.causw.app.main.domain.community.reaction.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.causw.app.main.domain.community.reaction.entity.LikePost;

public interface LikePostRepository extends JpaRepository<LikePost, String> {

	Boolean existsByPostIdAndUserId(String postId, String userId);

	void deleteLikeByPostIdAndUserId(String postId, String userId);

	Long countByPostId(String postId);

	@Query("SELECT lp.post.id FROM LikePost lp WHERE lp.user.id = :userId AND lp.post.id IN :postIds")
	Set<String> findLikedPostIdsByUserIdAndPostIds(@Param("userId") String userId,
		@Param("postIds") List<String> postIds);

	@Query("""
		SELECT lp
		FROM LikePost lp
		JOIN lp.post p
		WHERE lp.user.id = :userId
		AND (:#{#blockedUserIds.size()} = 0 OR p.writer.id NOT IN :blockedUserIds)
		ORDER BY p.createdAt DESC
		""")
	@EntityGraph(attributePaths = {"post"})
	Page<LikePost> findByUserId(@Param("userId") String userId, @Param("blockedUserIds") Set<String> blockedUserIds,
		Pageable pageable);
}
