package net.causw.adapter.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> findByPost_IdAndParentCommentIsNullOrderByCreatedAtDesc(String postId, Pageable pageable);

    Long countByPost_Id(String postId);
}
