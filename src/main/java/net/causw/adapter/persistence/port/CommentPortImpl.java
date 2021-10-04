package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Comment;
import net.causw.adapter.persistence.CommentRepository;
import net.causw.adapter.persistence.User;
import net.causw.application.spi.CommentPort;
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CommentPortImpl implements CommentPort {
    private final CommentRepository commentRepository;

    public CommentPortImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Optional<CommentDomainModel> findById(String id) {
        return this.commentRepository.findById(id).map(this::entityToDomainModelWithChild);
    }

    @Override
    public List<CommentDomainModel> findByPostId(String postId) {
        return this.commentRepository.findByPostId(postId)
                .stream()
                .map(this::entityToDomainModelWithChild)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDomainModel create(CommentDomainModel commentDomainModel, PostDomainModel postDomainModel) {
        return this.entityToDomainModelWithParent(this.commentRepository.save(Comment.from(commentDomainModel, postDomainModel)));
    }

    private CommentDomainModel entityToDomainModelWithChild(Comment comment) {
        return CommentDomainModel.of(
                comment.getId(),
                comment.getContent(),
                comment.getIsDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                this.entityToDomainModel(comment.getWriter()),
                comment.getPost().getId(),
                comment.getChildCommentList()
                        .stream()
                        .map(this::entityToDomainModel)
                        .collect(Collectors.toList())
        );
    }

    private CommentDomainModel entityToDomainModelWithParent(Comment comment) {
        CommentDomainModel parentCommentDomainModel = null;
        if (comment.getParentComment() != null) {
            parentCommentDomainModel = this.entityToDomainModel(comment.getParentComment());
        }

        return CommentDomainModel.of(
                comment.getId(),
                comment.getContent(),
                comment.getIsDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                this.entityToDomainModel(comment.getWriter()),
                comment.getPost().getId(),
                parentCommentDomainModel
        );
    }

    private CommentDomainModel entityToDomainModel(Comment comment) {
        return CommentDomainModel.of(
                comment.getId(),
                comment.getContent(),
                comment.getIsDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                this.entityToDomainModel(comment.getWriter()),
                comment.getPost().getId()
        );
    }

    private UserDomainModel entityToDomainModel(User user) {
        return UserDomainModel.of(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState()
        );
    }
}
