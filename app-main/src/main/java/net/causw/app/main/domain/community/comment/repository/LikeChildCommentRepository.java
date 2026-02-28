package net.causw.app.main.domain.community.comment.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.causw.app.main.domain.community.comment.entity.LikeChildComment;
import net.causw.app.main.domain.community.comment.repository.query.ChildCommentLikeCountDto;

public interface LikeChildCommentRepository extends JpaRepository<LikeChildComment, Long> {
	Boolean existsByChildCommentIdAndUserId(String childCommentId, String userId);

	Long countByChildCommentId(String childCommentId);

	void deleteLikeByChildCommentIdAndUserId(String childCommentId, String userId);

	@Query("SELECT l.childComment.id AS childCommentId, COUNT(l) AS likeCount " +
		"FROM LikeChildComment l " +
		"WHERE l.childComment.id IN :childCommentIds " +
		"GROUP BY l.childComment.id")
	List<ChildCommentLikeCountDto> countLikesByChildCommentIds(
		@Param("childCommentIds") List<String> childCommentIds);

	@Query("SELECT l.childComment.id " +
		"FROM LikeChildComment l " +
		"WHERE l.user.id = :userId " +
		"AND l.childComment.id IN :childCommentIds")
	Set<String> findLikedChildCommentIdsByUserIdAndChildCommentIds(
		@Param("userId") String userId,
		@Param("childCommentIds") List<String> childCommentIds);

}
