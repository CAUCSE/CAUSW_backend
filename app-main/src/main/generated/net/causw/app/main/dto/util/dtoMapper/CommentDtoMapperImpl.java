package net.causw.app.main.dto.util.dtoMapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.notification.UserCommentSubscribe;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserProfileImage;
import net.causw.app.main.dto.comment.ChildCommentResponseDto;
import net.causw.app.main.dto.comment.ChildCommentResponseDto.ChildCommentResponseDtoBuilder;
import net.causw.app.main.dto.comment.CommentResponseDto;
import net.causw.app.main.dto.comment.CommentResponseDto.CommentResponseDtoBuilder;
import net.causw.app.main.dto.comment.CommentSubscribeResponseDto;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:46+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
@Component
public class CommentDtoMapperImpl implements CommentDtoMapper {

    @Override
    public CommentResponseDto toCommentResponseDto(Comment comment, Long numChildComment, Long numCommentLike, Boolean isCommentLike, Boolean isOwner, List<ChildCommentResponseDto> childCommentList, Boolean updatable, Boolean deletable, Boolean isCommentSubscribed) {
        if ( comment == null && numChildComment == null && numCommentLike == null && isCommentLike == null && isOwner == null && childCommentList == null && updatable == null && deletable == null && isCommentSubscribed == null ) {
            return null;
        }

        CommentResponseDtoBuilder commentResponseDto = CommentResponseDto.builder();

        if ( comment != null ) {
            commentResponseDto.writerName( commentWriterName( comment ) );
            commentResponseDto.writerNickname( commentWriterNickname( comment ) );
            commentResponseDto.writerAdmissionYear( commentWriterAdmissionYear( comment ) );
            commentResponseDto.writerProfileImage( mapUuidFileToFileUrl( commentWriterUserProfileImage( comment ) ) );
            commentResponseDto.postId( commentPostId( comment ) );
            commentResponseDto.isAnonymous( comment.getIsAnonymous() );
            commentResponseDto.id( comment.getId() );
            commentResponseDto.content( comment.getContent() );
            commentResponseDto.createdAt( comment.getCreatedAt() );
            commentResponseDto.updatedAt( comment.getUpdatedAt() );
            commentResponseDto.isDeleted( comment.getIsDeleted() );
        }
        if ( numChildComment != null ) {
            commentResponseDto.numChildComment( numChildComment );
        }
        if ( numCommentLike != null ) {
            commentResponseDto.numLike( numCommentLike );
        }
        if ( isCommentLike != null ) {
            commentResponseDto.isCommentLike( isCommentLike );
        }
        if ( isOwner != null ) {
            commentResponseDto.isOwner( isOwner );
        }
        if ( childCommentList != null ) {
            List<ChildCommentResponseDto> list = childCommentList;
            if ( list != null ) {
                commentResponseDto.childCommentList( new ArrayList<ChildCommentResponseDto>( list ) );
            }
        }
        if ( updatable != null ) {
            commentResponseDto.updatable( updatable );
        }
        if ( deletable != null ) {
            commentResponseDto.deletable( deletable );
        }
        if ( isCommentSubscribed != null ) {
            commentResponseDto.isCommentSubscribed( isCommentSubscribed );
        }

        return commentResponseDto.build();
    }

    @Override
    public ChildCommentResponseDto toChildCommentResponseDto(ChildComment childComment, Long numChildCommentLike, Boolean isChildCommentLike, Boolean isOwner, Boolean updatable, Boolean deletable) {
        if ( childComment == null && numChildCommentLike == null && isChildCommentLike == null && isOwner == null && updatable == null && deletable == null ) {
            return null;
        }

        ChildCommentResponseDtoBuilder childCommentResponseDto = ChildCommentResponseDto.builder();

        if ( childComment != null ) {
            childCommentResponseDto.writerName( childCommentWriterName( childComment ) );
            childCommentResponseDto.writerNickname( childCommentWriterNickname( childComment ) );
            childCommentResponseDto.writerAdmissionYear( childCommentWriterAdmissionYear( childComment ) );
            childCommentResponseDto.writerProfileImage( mapUuidFileToFileUrl( childCommentWriterUserProfileImage( childComment ) ) );
            childCommentResponseDto.isAnonymous( childComment.getIsAnonymous() );
            childCommentResponseDto.id( childComment.getId() );
            childCommentResponseDto.content( childComment.getContent() );
            childCommentResponseDto.createdAt( childComment.getCreatedAt() );
            childCommentResponseDto.updatedAt( childComment.getUpdatedAt() );
            childCommentResponseDto.isDeleted( childComment.getIsDeleted() );
        }
        if ( numChildCommentLike != null ) {
            childCommentResponseDto.numLike( numChildCommentLike );
        }
        if ( isChildCommentLike != null ) {
            childCommentResponseDto.isChildCommentLike( isChildCommentLike );
        }
        if ( isOwner != null ) {
            childCommentResponseDto.isOwner( isOwner );
        }
        if ( updatable != null ) {
            childCommentResponseDto.updatable( updatable );
        }
        if ( deletable != null ) {
            childCommentResponseDto.deletable( deletable );
        }

        return childCommentResponseDto.build();
    }

    @Override
    public CommentSubscribeResponseDto toCommentSubscribeResponseDto(UserCommentSubscribe userCommentSubscribe) {
        if ( userCommentSubscribe == null ) {
            return null;
        }

        CommentSubscribeResponseDto commentSubscribeResponseDto = new CommentSubscribeResponseDto();

        commentSubscribeResponseDto.setCommentId( userCommentSubscribeCommentId( userCommentSubscribe ) );
        commentSubscribeResponseDto.setUserId( userCommentSubscribeUserId( userCommentSubscribe ) );
        commentSubscribeResponseDto.setIsSubscribed( userCommentSubscribe.getIsSubscribed() );

        return commentSubscribeResponseDto;
    }

    private String commentWriterName(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        User writer = comment.getWriter();
        if ( writer == null ) {
            return null;
        }
        String name = writer.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String commentWriterNickname(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        User writer = comment.getWriter();
        if ( writer == null ) {
            return null;
        }
        String nickname = writer.getNickname();
        if ( nickname == null ) {
            return null;
        }
        return nickname;
    }

    private Integer commentWriterAdmissionYear(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        User writer = comment.getWriter();
        if ( writer == null ) {
            return null;
        }
        Integer admissionYear = writer.getAdmissionYear();
        if ( admissionYear == null ) {
            return null;
        }
        return admissionYear;
    }

    private UserProfileImage commentWriterUserProfileImage(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        User writer = comment.getWriter();
        if ( writer == null ) {
            return null;
        }
        UserProfileImage userProfileImage = writer.getUserProfileImage();
        if ( userProfileImage == null ) {
            return null;
        }
        return userProfileImage;
    }

    private String commentPostId(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        Post post = comment.getPost();
        if ( post == null ) {
            return null;
        }
        String id = post.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String childCommentWriterName(ChildComment childComment) {
        if ( childComment == null ) {
            return null;
        }
        User writer = childComment.getWriter();
        if ( writer == null ) {
            return null;
        }
        String name = writer.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String childCommentWriterNickname(ChildComment childComment) {
        if ( childComment == null ) {
            return null;
        }
        User writer = childComment.getWriter();
        if ( writer == null ) {
            return null;
        }
        String nickname = writer.getNickname();
        if ( nickname == null ) {
            return null;
        }
        return nickname;
    }

    private Integer childCommentWriterAdmissionYear(ChildComment childComment) {
        if ( childComment == null ) {
            return null;
        }
        User writer = childComment.getWriter();
        if ( writer == null ) {
            return null;
        }
        Integer admissionYear = writer.getAdmissionYear();
        if ( admissionYear == null ) {
            return null;
        }
        return admissionYear;
    }

    private UserProfileImage childCommentWriterUserProfileImage(ChildComment childComment) {
        if ( childComment == null ) {
            return null;
        }
        User writer = childComment.getWriter();
        if ( writer == null ) {
            return null;
        }
        UserProfileImage userProfileImage = writer.getUserProfileImage();
        if ( userProfileImage == null ) {
            return null;
        }
        return userProfileImage;
    }

    private String userCommentSubscribeCommentId(UserCommentSubscribe userCommentSubscribe) {
        if ( userCommentSubscribe == null ) {
            return null;
        }
        Comment comment = userCommentSubscribe.getComment();
        if ( comment == null ) {
            return null;
        }
        String id = comment.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String userCommentSubscribeUserId(UserCommentSubscribe userCommentSubscribe) {
        if ( userCommentSubscribe == null ) {
            return null;
        }
        User user = userCommentSubscribe.getUser();
        if ( user == null ) {
            return null;
        }
        String id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
