package net.causw.application.spi;

import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface CommentPort {
    Optional<CommentDomainModel> findById(String id);

    Page<CommentDomainModel> findByPostId(String postId, Integer pageNum);

    Long countByPostId(String postId);

    CommentDomainModel create(CommentDomainModel commentDomainModel, PostDomainModel postDomainModel);

    Optional<CommentDomainModel> delete(String commentId);
}
