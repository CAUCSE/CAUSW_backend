package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Comment;
import net.causw.adapter.persistence.CommentRepository;
import net.causw.adapter.persistence.PageableFactory;
import net.causw.application.spi.CommentPort;
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommentPortImpl extends DomainModelMapper implements CommentPort {
    private final CommentRepository commentRepository;
    private final PageableFactory pageableFactory;

    public CommentPortImpl(
            CommentRepository commentRepository,
            PageableFactory pageableFactory
    ) {
        this.commentRepository = commentRepository;
        this.pageableFactory = pageableFactory;
    }

    @Override
    public Optional<CommentDomainModel> findById(String id) {
        return this.commentRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public Page<CommentDomainModel> findByPostId(String postId, Integer pageNum) {
        Page<Comment> comments = this.commentRepository.findByPost_IdOrderByCreatedAtDesc(postId, this.pageableFactory.create(pageNum));

        return comments
                .map(this::entityToDomainModel);
    }

    @Override
    public Long countByPostId(String postId) {
        return this.commentRepository.countByPost_IdAndIsDeletedIsFalse(postId);
    }

    @Override
    public CommentDomainModel create(CommentDomainModel commentDomainModel, PostDomainModel postDomainModel) {
        return this.entityToDomainModel(this.commentRepository.save(Comment.from(commentDomainModel, postDomainModel)));
    }

    @Override
    public Optional<CommentDomainModel> update(String commentId, CommentDomainModel commentDomainModel) {
        return this.commentRepository.findById(commentId).map(
                srcComment -> {
                    srcComment.setContent(commentDomainModel.getContent());

                    return this.entityToDomainModel(this.commentRepository.save(srcComment));
                }
        );
    }

    @Override
    public Optional<CommentDomainModel> delete(String commentId) {
        return this.commentRepository.findById(commentId).map(
                comment -> {
                    comment.setIsDeleted(true);

                    return this.entityToDomainModel(this.commentRepository.save(comment));
                }
        );
    }

    @Override
    public Page<CommentDomainModel> findByUserId(String userId, Integer pageNum) {
        return this.commentRepository.findByUserId(userId, this.pageableFactory.create(pageNum)).map(this::entityToDomainModel);
    }
}
