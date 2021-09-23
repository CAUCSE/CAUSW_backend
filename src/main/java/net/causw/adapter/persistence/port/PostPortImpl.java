package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Board;
import net.causw.adapter.persistence.Circle;
import net.causw.adapter.persistence.Post;
import net.causw.adapter.persistence.PostRepository;
import net.causw.adapter.persistence.User;
import net.causw.application.spi.PostPort;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Component
public class PostPortImpl implements PostPort {
    private final PostRepository postRepository;

    public PostPortImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public Optional<PostDomainModel> findById(String id) {
        return this.postRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public PostDomainModel create(PostDomainModel postDomainModel) {
        return this.entityToDomainModel(this.postRepository.save(Post.from(postDomainModel)));
    }

    private PostDomainModel entityToDomainModel(Post post) {
        return PostDomainModel.of(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                this.entityToDomainModel(post.getWriter()),
                post.getIsDeleted(),
                this.entityToDomainModel(post.getBoard()),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private BoardDomainModel entityToDomainModel(Board board) {
        CircleDomainModel circleDomainModel = null;
        if (board.getCircle() != null) {
            circleDomainModel = this.entityToDomainModel(board.getCircle());
        }

        return BoardDomainModel.of(
                board.getId(),
                board.getName(),
                board.getDescription(),
                new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))),
                new ArrayList<>(Arrays.asList(board.getModifyRoles().split(","))),
                new ArrayList<>(Arrays.asList(board.getReadRoles().split(","))),
                board.getIsDeleted(),
                circleDomainModel
        );
    }

    private CircleDomainModel entityToDomainModel(Circle circle) {
        return CircleDomainModel.of(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                this.entityToDomainModel(circle.getLeader())
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
