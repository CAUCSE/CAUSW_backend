package net.causw.application.spi;

import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;

import java.util.List;
import java.util.Optional;

public interface CommentPort {
    Optional<CommentDomainModel> findById(String id);

    // TODO : GHJANG : Pagination
    List<CommentDomainModel> findByPostId(String postId);

    CommentDomainModel create(CommentDomainModel commentDomainModel, PostDomainModel postDomainModel);
}
