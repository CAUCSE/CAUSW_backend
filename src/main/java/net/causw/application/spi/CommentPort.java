package net.causw.application.spi;

import net.causw.domain.model.comment.CommentDomainModel;
import net.causw.domain.model.post.PostDomainModel;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface CommentPort {
    Optional<CommentDomainModel> findById(String id);

    Page<CommentDomainModel> findByPostId(String postId, Integer pageNum);

    CommentDomainModel create(CommentDomainModel commentDomainModel, PostDomainModel postDomainModel);

    Optional<CommentDomainModel> update(String commentId, CommentDomainModel commentDomainModel);

    Optional<CommentDomainModel> delete(String commentId);

    Page<CommentDomainModel> findByUserId(String userId, Integer pageNum);
}
