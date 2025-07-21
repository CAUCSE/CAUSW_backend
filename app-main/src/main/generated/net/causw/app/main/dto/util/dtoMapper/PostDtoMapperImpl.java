package net.causw.app.main.dto.util.dtoMapper;

import java.util.Set;
import javax.annotation.processing.Generated;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.notification.UserPostSubscribe;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.PostAttachImage;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserProfileImage;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.dto.comment.CommentResponseDto;
import net.causw.app.main.dto.form.response.FormResponseDto;
import net.causw.app.main.dto.post.BoardPostsResponseDto;
import net.causw.app.main.dto.post.BoardPostsResponseDto.BoardPostsResponseDtoBuilder;
import net.causw.app.main.dto.post.PostContentDto;
import net.causw.app.main.dto.post.PostCreateResponseDto;
import net.causw.app.main.dto.post.PostCreateResponseDto.PostCreateResponseDtoBuilder;
import net.causw.app.main.dto.post.PostResponseDto;
import net.causw.app.main.dto.post.PostResponseDto.PostResponseDtoBuilder;
import net.causw.app.main.dto.post.PostSubscribeResponseDto;
import net.causw.app.main.dto.post.PostsResponseDto;
import net.causw.app.main.dto.post.PostsResponseDto.PostsResponseDtoBuilder;
import net.causw.app.main.dto.vote.VoteResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:46+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
@Component
public class PostDtoMapperImpl implements PostDtoMapper {

    @Override
    public PostsResponseDto toPostsResponseDto(Post post, Long numComment, Long numPostLike, Long numPostFavorite, PostAttachImage thumbnail, Boolean isPostVote, Boolean isPostForm) {
        if ( post == null && numComment == null && numPostLike == null && numPostFavorite == null && thumbnail == null && isPostVote == null && isPostForm == null ) {
            return null;
        }

        PostsResponseDtoBuilder postsResponseDto = PostsResponseDto.builder();

        if ( post != null ) {
            postsResponseDto.id( post.getId() );
            postsResponseDto.writerName( postWriterName( post ) );
            postsResponseDto.writerNickname( postWriterNickname( post ) );
            postsResponseDto.writerAdmissionYear( postWriterAdmissionYear( post ) );
            postsResponseDto.content( post.getContent() );
            postsResponseDto.isAnonymous( post.getIsAnonymous() );
            postsResponseDto.isQuestion( post.getIsQuestion() );
            postsResponseDto.createdAt( post.getCreatedAt() );
            postsResponseDto.updatedAt( post.getUpdatedAt() );
            postsResponseDto.title( post.getTitle() );
            postsResponseDto.isDeleted( post.getIsDeleted() );
        }
        if ( numComment != null ) {
            postsResponseDto.numComment( numComment );
        }
        if ( numPostLike != null ) {
            postsResponseDto.numLike( numPostLike );
        }
        if ( numPostFavorite != null ) {
            postsResponseDto.numFavorite( numPostFavorite );
        }
        if ( thumbnail != null ) {
            postsResponseDto.postAttachImage( mapUuidFileToFileUrl( thumbnail ) );
        }
        if ( isPostVote != null ) {
            postsResponseDto.isPostVote( isPostVote );
        }
        if ( isPostForm != null ) {
            postsResponseDto.isPostForm( isPostForm );
        }

        return postsResponseDto.build();
    }

    @Override
    public PostResponseDto toPostResponseDtoExtended(Post post, Page<CommentResponseDto> commentList, Long numComment, Long numPostLike, Long numPostFavorite, Boolean isPostLike, Boolean isPostFavorite, Boolean isOwner, Boolean updatable, Boolean deletable, FormResponseDto formResponseDto, VoteResponseDto voteResponseDto, Boolean isPostVote, Boolean isPostForm, Boolean isPostSubscribed) {
        if ( post == null && commentList == null && numComment == null && numPostLike == null && numPostFavorite == null && isPostLike == null && isPostFavorite == null && isOwner == null && updatable == null && deletable == null && formResponseDto == null && voteResponseDto == null && isPostVote == null && isPostForm == null && isPostSubscribed == null ) {
            return null;
        }

        PostResponseDtoBuilder postResponseDto = PostResponseDto.builder();

        if ( post != null ) {
            postResponseDto.title( post.getTitle() );
            postResponseDto.writerName( postWriterName( post ) );
            postResponseDto.writerNickname( postWriterNickname( post ) );
            postResponseDto.writerAdmissionYear( postWriterAdmissionYear( post ) );
            postResponseDto.boardName( postBoardName( post ) );
            postResponseDto.fileUrlList( mapUuidFileListToFileUrlList( post.getPostAttachImageList() ) );
            postResponseDto.content( post.getContent() );
            postResponseDto.isAnonymous( post.getIsAnonymous() );
            postResponseDto.isQuestion( post.getIsQuestion() );
            postResponseDto.writerProfileImage( mapUuidFileToFileUrl( postWriterUserProfileImage( post ) ) );
            postResponseDto.id( post.getId() );
            postResponseDto.isDeleted( post.getIsDeleted() );
            postResponseDto.createdAt( post.getCreatedAt() );
            postResponseDto.updatedAt( post.getUpdatedAt() );
        }
        if ( commentList != null ) {
            postResponseDto.commentList( commentList );
        }
        if ( numComment != null ) {
            postResponseDto.numComment( numComment );
        }
        if ( numPostLike != null ) {
            postResponseDto.numLike( numPostLike );
        }
        if ( numPostFavorite != null ) {
            postResponseDto.numFavorite( numPostFavorite );
        }
        if ( isPostLike != null ) {
            postResponseDto.isPostLike( isPostLike );
        }
        if ( isPostFavorite != null ) {
            postResponseDto.isPostFavorite( isPostFavorite );
        }
        if ( isOwner != null ) {
            postResponseDto.isOwner( isOwner );
        }
        if ( updatable != null ) {
            postResponseDto.updatable( updatable );
        }
        if ( deletable != null ) {
            postResponseDto.deletable( deletable );
        }
        if ( formResponseDto != null ) {
            postResponseDto.formResponseDto( formResponseDto );
        }
        if ( voteResponseDto != null ) {
            postResponseDto.voteResponseDto( voteResponseDto );
        }
        if ( isPostVote != null ) {
            postResponseDto.isPostVote( isPostVote );
        }
        if ( isPostForm != null ) {
            postResponseDto.isPostForm( isPostForm );
        }
        if ( isPostSubscribed != null ) {
            postResponseDto.isPostSubscribed( isPostSubscribed );
        }

        return postResponseDto.build();
    }

    @Override
    public PostContentDto toPostContentDto(Post post) {
        if ( post == null ) {
            return null;
        }

        PostContentDto postContentDto = new PostContentDto();

        postContentDto.setTitle( post.getTitle() );
        postContentDto.setContentId( post.getId() );

        return postContentDto;
    }

    @Override
    public BoardPostsResponseDto toBoardPostsResponseDto(Board board, Set<Role> userRole, Boolean writable, Boolean isFavorite, Boolean isBoardSubscribed, Page<PostsResponseDto> post) {
        if ( board == null && userRole == null && writable == null && isFavorite == null && isBoardSubscribed == null && post == null ) {
            return null;
        }

        BoardPostsResponseDtoBuilder boardPostsResponseDto = BoardPostsResponseDto.builder();

        if ( board != null ) {
            boardPostsResponseDto.boardId( board.getId() );
            boardPostsResponseDto.boardName( board.getName() );
        }
        if ( writable != null ) {
            boardPostsResponseDto.writable( writable );
        }
        if ( isFavorite != null ) {
            boardPostsResponseDto.isFavorite( isFavorite );
        }
        if ( isBoardSubscribed != null ) {
            boardPostsResponseDto.isBoardSubscribed( isBoardSubscribed );
        }
        if ( post != null ) {
            boardPostsResponseDto.post( post );
        }

        return boardPostsResponseDto.build();
    }

    @Override
    public PostCreateResponseDto toPostCreateResponseDto(Post post) {
        if ( post == null ) {
            return null;
        }

        PostCreateResponseDtoBuilder postCreateResponseDto = PostCreateResponseDto.builder();

        postCreateResponseDto.id( post.getId() );

        return postCreateResponseDto.build();
    }

    @Override
    public PostSubscribeResponseDto toPostSubscribeResponseDto(UserPostSubscribe userPostSubscribe) {
        if ( userPostSubscribe == null ) {
            return null;
        }

        PostSubscribeResponseDto postSubscribeResponseDto = new PostSubscribeResponseDto();

        postSubscribeResponseDto.setPostId( userPostSubscribePostId( userPostSubscribe ) );
        postSubscribeResponseDto.setUserId( userPostSubscribeUserId( userPostSubscribe ) );
        postSubscribeResponseDto.setIsSubscribed( userPostSubscribe.getIsSubscribed() );

        return postSubscribeResponseDto;
    }

    private String postWriterName(Post post) {
        if ( post == null ) {
            return null;
        }
        User writer = post.getWriter();
        if ( writer == null ) {
            return null;
        }
        String name = writer.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String postWriterNickname(Post post) {
        if ( post == null ) {
            return null;
        }
        User writer = post.getWriter();
        if ( writer == null ) {
            return null;
        }
        String nickname = writer.getNickname();
        if ( nickname == null ) {
            return null;
        }
        return nickname;
    }

    private Integer postWriterAdmissionYear(Post post) {
        if ( post == null ) {
            return null;
        }
        User writer = post.getWriter();
        if ( writer == null ) {
            return null;
        }
        Integer admissionYear = writer.getAdmissionYear();
        if ( admissionYear == null ) {
            return null;
        }
        return admissionYear;
    }

    private String postBoardName(Post post) {
        if ( post == null ) {
            return null;
        }
        Board board = post.getBoard();
        if ( board == null ) {
            return null;
        }
        String name = board.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private UserProfileImage postWriterUserProfileImage(Post post) {
        if ( post == null ) {
            return null;
        }
        User writer = post.getWriter();
        if ( writer == null ) {
            return null;
        }
        UserProfileImage userProfileImage = writer.getUserProfileImage();
        if ( userProfileImage == null ) {
            return null;
        }
        return userProfileImage;
    }

    private String userPostSubscribePostId(UserPostSubscribe userPostSubscribe) {
        if ( userPostSubscribe == null ) {
            return null;
        }
        Post post = userPostSubscribe.getPost();
        if ( post == null ) {
            return null;
        }
        String id = post.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String userPostSubscribeUserId(UserPostSubscribe userPostSubscribe) {
        if ( userPostSubscribe == null ) {
            return null;
        }
        User user = userPostSubscribe.getUser();
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
