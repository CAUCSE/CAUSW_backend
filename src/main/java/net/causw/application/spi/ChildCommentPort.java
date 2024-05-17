package net.causw.application.spi;

import net.causw.domain.model.comment.ChildCommentDomainModel;
import net.causw.domain.model.post.PostDomainModel;

import java.util.Optional;

public interface ChildCommentPort {
    Optional<ChildCommentDomainModel> findById(String id);

    Long countByParentComment(String parentCommentId);

    ChildCommentDomainModel create(ChildCommentDomainModel childCommentDomainModel, PostDomainModel postDomainModel);

    Optional<ChildCommentDomainModel> update(String childCommentId, ChildCommentDomainModel childCommentDomainModel);

    Optional<ChildCommentDomainModel> delete(String childCommentId);
}
