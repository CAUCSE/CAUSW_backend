package net.causw.app.main.repository.comment;

import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.post.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChildCommentRepository extends JpaRepository<ChildComment, String> {
	Page<ChildComment> findByParentComment_IdOrderByCreatedAtAsc(String parentCommentId, Pageable pageable);

	Long countByParentComment_IdAndIsDeletedIsFalse(String parentCommentId);

	@Query("select c from ChildComment c where c.parentComment.id = :parentCommentId order by c.createdAt asc")
	List<ChildComment> findByParentComment_Id(@Param("parentCommentId") String parentCommentId);

	@Query("SELECT DISTINCT p " +
		"FROM ChildComment cc " +
		"JOIN cc.parentComment c " +
		"JOIN c.post p " +
		"WHERE cc.writer.id = :userId AND cc.isDeleted = false " +
		"ORDER BY p.createdAt DESC")
	Page<Post> findPostsByUserId(@Param("userId") String userId, Pageable pageable);
}
