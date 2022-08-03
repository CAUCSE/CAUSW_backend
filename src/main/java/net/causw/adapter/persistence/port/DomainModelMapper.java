package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.*;
import net.causw.domain.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
                post.getUpdatedAt(),
                post.getAttachments().map(attachments -> Arrays.asList(attachments.split(":::"))).orElse(List.of())
        );
    }

    protected BoardDomainModel entityToDomainModel(Board board) {
        return BoardDomainModel.of(
                board.getId(),
                board.getName(),
                board.getDescription(),
                new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))),
                board.getCategory(),
                board.getIsDeleted(),
                Optional.ofNullable(board.getCircle()).map(this::entityToDomainModel).orElse(null)
        );
    }

    protected CircleDomainModel entityToDomainModel(Circle circle) {
        return CircleDomainModel.of(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                circle.getLeader().map(this::entityToDomainModel).orElse(null),
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
                lockerLocation.getName()
        );
    }

    protected FavoriteBoardDomainModel entityToDomainModel(FavoriteBoard favoriteBoard) {
        return FavoriteBoardDomainModel.of(
                favoriteBoard.getId(),
                this.entityToDomainModel(favoriteBoard.getUser()),
                this.entityToDomainModel(favoriteBoard.getBoard())
        );
    }

    protected ChildCommentDomainModel entityToDomainModel(ChildComment comment) {
        return ChildCommentDomainModel.of(
                comment.getId(),
                comment.getContent(),
                comment.getIsDeleted(),
                comment.getTagUserName(),
                comment.getRefChildComment(),
                this.entityToDomainModel(comment.getWriter()),
                this.entityToDomainModel(comment.getParentComment()),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
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

    protected DeviceTokenDomainModel entityToDomainModel(DeviceToken deviceToken) {
        return DeviceTokenDomainModel.of(
                deviceToken.getId(),
                deviceToken.getDeviceToken(),
                deviceToken.getOs(),
                deviceToken.getDeviceName(),
                this.entityToDomainModel(deviceToken.getUser())
        );
    }
}
