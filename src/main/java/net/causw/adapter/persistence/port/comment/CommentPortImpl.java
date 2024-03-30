package net.causw.adapter.persistence.port.comment;

import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.port.mapper.DomainModelMapper;
import net.causw.adapter.persistence.repository.ChildCommentRepository;
import net.causw.adapter.persistence.repository.CommentRepository;
import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.application.spi.CommentPort;
import net.causw.domain.model.comment.CommentDomainModel;
import net.causw.domain.model.post.PostDomainModel;
import net.causw.domain.model.util.StaticValue;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CommentPortImpl extends DomainModelMapper implements CommentPort {
    private final CommentRepository commentRepository;
    private final ChildCommentRepository childCommentRepository;
    private final PageableFactory pageableFactory;

    public CommentPortImpl(
            CommentRepository commentRepository, ChildCommentRepository childCommentRepository,
            PageableFactory pageableFactory
    ) {
        this.commentRepository = commentRepository;
        this.childCommentRepository = childCommentRepository;
        this.pageableFactory = pageableFactory;
    }

    @Override
    public Optional<CommentDomainModel> findById(String id) {
        return this.commentRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public Page<CommentDomainModel> findByPostId(String postId, Integer pageNum) {
        Page<CommentDomainModel> commentDomainModels = this.commentRepository.findByPost_IdOrderByCreatedAt(
                postId,
                this.pageableFactory.create(pageNum, StaticValue.DEFAULT_COMMENT_PAGE_SIZE)
        ).map(this::entityToDomainModel);

        for (CommentDomainModel commentDomainModel : commentDomainModels) {
            commentDomainModel.setChildCommentList(
                    this.childCommentRepository.findByParentComment_Id(commentDomainModel.getId()).stream()
                            .map(this::entityToDomainModel)
                            .collect(Collectors.toList())
            );
        }

        return commentDomainModels;
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
        return this.commentRepository.findByUserId(userId, this.pageableFactory.create(pageNum, StaticValue.DEFAULT_COMMENT_PAGE_SIZE))
                .map(this::entityToDomainModel);
    }
}
