package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Comment;
import net.causw.adapter.persistence.CommentRepository;
import net.causw.adapter.persistence.Post;
import net.causw.adapter.persistence.User;
import net.causw.application.spi.CommentPort;
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Component;

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
        return this.commentRepository.findById(id).map(this::entityToDomainModelWithChildren);
    }

    @Override
    public CommentDomainModel create(
            CommentDomainModel commentDomainModel,
            UserDomainModel writer,
            PostDomainModel post
    ) {
        return this.entityToDomainModelWithParent(
                this.commentRepository.save(Comment.of(
                        commentDomainModel.getContent(),
                        false,
                        User.from(writer),
                        Post.from(post),
                        null
                ))
        );
    }

    @Override
    public CommentDomainModel create(
            CommentDomainModel commentDomainModel,
            UserDomainModel writer,
            PostDomainModel post,
            CommentDomainModel parentComment
    ) {
        return this.entityToDomainModelWithParent(
                this.commentRepository.save(Comment.of(
                    commentDomainModel.getContent(),
                    false,
                    User.from(writer),
                    Post.from(post),
                    Comment.from(parentComment)
                ))
        );
    }

    private CommentDomainModel entityToDomainModelWithParent(Comment comment) {
        return CommentDomainModel.of(
                comment.getId(),
                comment.getContent(),
                comment.getIsDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                this.entityToDomainModel(comment.getWriter()),
                this.entityToDomainModel(comment.getPost()),
                this.entityToDomainModel(comment.getParentComment())
        );
    }

    private CommentDomainModel entityToDomainModelWithChildren(Comment comment) {
        return CommentDomainModel.of(
                comment.getId(),
                comment.getContent(),
                comment.getIsDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                this.entityToDomainModel(comment.getWriter()),
                this.entityToDomainModel(comment.getPost()),
                comment.getChildCommentList().stream().map(this::entityToDomainModel).collect(Collectors.toList())
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
                this.entityToDomainModel(comment.getPost())
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

    private PostDomainModel entityToDomainModel(Post post) {
        return PostDomainModel.of(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsDeleted(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getBoard().getId()
        );
    }
}
