package net.causw.application.spi;

import net.causw.domain.model.ChildCommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface ChildCommentPort {
    Optional<ChildCommentDomainModel> findById(String id);

    Page<ChildCommentDomainModel> findByParentComment(String parentCommentId, Integer pageNum);

    Long countByParentComment(String parentCommentId);

    ChildCommentDomainModel create(ChildCommentDomainModel childCommentDomainModel, PostDomainModel postDomainModel);

    Optional<ChildCommentDomainModel> update(String childCommentId, ChildCommentDomainModel childCommentDomainModel);

    Optional<ChildCommentDomainModel> delete(String childCommentId);
}
