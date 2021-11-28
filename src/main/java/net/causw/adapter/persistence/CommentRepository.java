package net.causw.adapter.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> findByPost_IdAndParentCommentIsNullOrderByCreatedAtAsc(String postId, Pageable pageable);

    Long countByPost_Id(String postId);
}
