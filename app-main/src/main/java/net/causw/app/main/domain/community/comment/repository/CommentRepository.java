package net.causw.app.main.domain.community.comment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.comment.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

	Optional<Comment> findByIdAndIsDeletedFalse(String commentId);

}
