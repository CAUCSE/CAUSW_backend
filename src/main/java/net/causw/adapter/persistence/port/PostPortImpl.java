package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Comment;
import net.causw.adapter.persistence.Post;
import net.causw.adapter.persistence.User;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.application.spi.PostPort;
import net.causw.adapter.persistence.PostRepository;
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PostPortImpl implements PostPort {
    private final PostRepository postRepository;

    public PostPortImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public PostDomainModel findById(String id) {
        return entityToDomainModelWithComment(this.postRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid post id"
                )
        ));
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

    private PostDomainModel entityToDomainModelWithComment(Post post) {
        return PostDomainModel.of(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getIsDeleted(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getBoard().getId(),
                post.getCommentList().stream().map(this::entityToDomainModel).collect(Collectors.toList())
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
                this.entityToDomainModel(comment.getPost()),
                comment.getChildCommentList().stream().map(this::entityToDomainModel).collect(Collectors.toList())
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
