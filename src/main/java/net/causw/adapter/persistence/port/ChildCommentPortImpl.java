package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.ChildComment;
import net.causw.adapter.persistence.ChildCommentRepository;
import net.causw.adapter.persistence.PageableFactory;
import net.causw.application.spi.ChildCommentPort;
import net.causw.domain.model.ChildCommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class ChildCommentPortImpl extends DomainModelMapper implements ChildCommentPort {
    private final ChildCommentRepository childCommentRepository;
    private final PageableFactory pageableFactory;

    public ChildCommentPortImpl(
            ChildCommentRepository childCommentRepository,
            PageableFactory pageableFactory
    ) {
        this.childCommentRepository = childCommentRepository;
        this.pageableFactory = pageableFactory;
    }

    @Override
    public Optional<ChildCommentDomainModel> findById(String id) {
        return this.childCommentRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public Page<ChildCommentDomainModel> findByParentComment(String parentCommentId, Integer pageNum) {
        Page<ChildComment> childComments = this.childCommentRepository.findByParentComment_IdOrderByCreatedAtDesc(parentCommentId, this.pageableFactory.create(pageNum));
        Collections.reverse(childComments.getContent());

        return childComments
                .map(this::entityToDomainModel);
    }

    @Override
    public Long countByParentComment(String parentCommentId) {
        return this.childCommentRepository.countByParentComment_IdAndIsDeletedIsFalse(parentCommentId);
    }

    @Override
    public ChildCommentDomainModel create(ChildCommentDomainModel childCommentDomainModel, PostDomainModel postDomainModel) {
        return this.entityToDomainModel(this.childCommentRepository.save(ChildComment.from(childCommentDomainModel, postDomainModel)));
    }

    @Override
    public Optional<ChildCommentDomainModel> update(String childCommentId, ChildCommentDomainModel childCommentDomainModel) {
        return this.childCommentRepository.findById(childCommentId).map(
                srcChildComment -> {
                    srcChildComment.setContent(childCommentDomainModel.getContent());
                    srcChildComment.setTagUserName(childCommentDomainModel.getTagUserName());
                    srcChildComment.setRefChildComment(childCommentDomainModel.getRefChildComment());

                    return this.entityToDomainModel(this.childCommentRepository.save(srcChildComment));
                }
        );
    }

    @Override
    public Optional<ChildCommentDomainModel> delete(String childCommentId) {
        return this.childCommentRepository.findById(childCommentId).map(
                childComment -> {
                    childComment.setIsDeleted(true);

                    return this.entityToDomainModel(this.childCommentRepository.save(childComment));
                }
        );
    }
}
