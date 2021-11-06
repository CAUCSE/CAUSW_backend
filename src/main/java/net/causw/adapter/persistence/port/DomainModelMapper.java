package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Board;
import net.causw.adapter.persistence.Circle;
import net.causw.adapter.persistence.CircleMember;
import net.causw.adapter.persistence.Comment;
import net.causw.adapter.persistence.FavoriteBoard;
import net.causw.adapter.persistence.Locker;
import net.causw.adapter.persistence.LockerLocation;
import net.causw.adapter.persistence.Post;
import net.causw.adapter.persistence.User;
import net.causw.adapter.persistence.UserAdmission;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.FavoriteBoardDomainModel;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.model.LockerLocationDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.UserAdmissionDomainModel;
import net.causw.domain.model.UserDomainModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

// TODO: Refactoring
public abstract class DomainModelMapper {
    protected UserDomainModel entityToDomainModel(User user) {
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

    protected UserAdmissionDomainModel entityToDomainModel(UserAdmission userAdmission) {
        return UserAdmissionDomainModel.of(
                userAdmission.getId(),
                this.entityToDomainModel(userAdmission.getUser()),
                userAdmission.getAttachImage(),
                userAdmission.getDescription(),
                userAdmission.getCreatedAt(),
                userAdmission.getUpdatedAt()
        );
    }

    protected PostDomainModel entityToDomainModel(Post post) {
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

    protected BoardDomainModel entityToDomainModel(Board board) {
        CircleDomainModel circleDomainModel = null;
        if (board.getCircle() != null) {
            circleDomainModel = this.entityToDomainModel(board.getCircle());
        }

        return BoardDomainModel.of(
                board.getId(),
                board.getName(),
                board.getDescription(),
                new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))),
                board.getCategory(),
                board.getIsDeleted(),
                circleDomainModel
        );
    }

    protected CircleDomainModel entityToDomainModel(Circle circle) {
        return CircleDomainModel.of(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                this.entityToDomainModel(circle.getLeader()),
                circle.getCreatedAt(),
                circle.getUpdatedAt()
        );
    }

    protected LockerDomainModel entityToDomainModel(Locker locker) {
        return LockerDomainModel.of(
                locker.getId(),
                locker.getLockerNumber(),
                locker.getIsActive(),
                locker.getUpdatedAt(),
                locker.getUser().map(this::entityToDomainModel).orElse(null),
                this.entityToDomainModel(locker.getLocation())
        );
    }

    protected LockerLocationDomainModel entityToDomainModel(LockerLocation lockerLocation) {
        return LockerLocationDomainModel.of(
                lockerLocation.getId(),
                lockerLocation.getName(),
                lockerLocation.getDescription()
        );
    }

    protected FavoriteBoardDomainModel entityToDomainModel(FavoriteBoard favoriteBoard) {
        return FavoriteBoardDomainModel.of(
                favoriteBoard.getId(),
                this.entityToDomainModel(favoriteBoard.getUser()),
                this.entityToDomainModel(favoriteBoard.getBoard())
        );
    }

    protected CommentDomainModel entityToDomainModelWithChild(Comment comment) {
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

    protected CommentDomainModel entityToDomainModelWithParent(Comment comment) {
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

    protected CommentDomainModel entityToDomainModel(Comment comment) {
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

    protected CircleMemberDomainModel entityToDomainModel(CircleMember circleMember) {
        return CircleMemberDomainModel.of(
                circleMember.getId(),
                circleMember.getStatus(),
                this.entityToDomainModel(circleMember.getCircle()),
                circleMember.getUser().getId(),
                circleMember.getUser().getName(),
                circleMember.getCreatedAt(),
                circleMember.getUpdatedAt()
        );
    }
}
