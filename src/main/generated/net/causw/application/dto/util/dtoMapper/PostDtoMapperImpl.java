package net.causw.application.dto.util.dtoMapper;

import java.util.Set;
import javax.annotation.processing.Generated;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.post.BoardPostsResponseDto;
import net.causw.application.dto.post.BoardPostsResponseDto.BoardPostsResponseDtoBuilder;
import net.causw.application.dto.post.PostContentDto;
import net.causw.application.dto.post.PostResponseDto;
import net.causw.application.dto.post.PostResponseDto.PostResponseDtoBuilder;
import net.causw.application.dto.post.PostsResponseDto;
import net.causw.application.dto.post.PostsResponseDto.PostsResponseDtoBuilder;
import net.causw.domain.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-13T05:58:22+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.12 (Azul Systems, Inc.)"
)
@Component
public class PostDtoMapperImpl implements PostDtoMapper {

    @Override
    public PostsResponseDto toPostsResponseDto(Post post, Long numComment, Long numPostLike, Long numPostFavorite) {
        if ( post == null && numComment == null && numPostLike == null && numPostFavorite == null ) {
            return null;
        }

        PostsResponseDtoBuilder postsResponseDto = PostsResponseDto.builder();

        if ( post != null ) {
            postsResponseDto.writerName( postWriterName( post ) );
            postsResponseDto.writerAdmissionYear( postWriterAdmissionYear( post ) );
            postsResponseDto.content( post.getContent() );
            postsResponseDto.isAnonymous( post.getIsAnonymous() );
            postsResponseDto.isQuestion( post.getIsQuestion() );
            postsResponseDto.id( post.getId() );
            postsResponseDto.title( post.getTitle() );
            postsResponseDto.createdAt( post.getCreatedAt() );
            postsResponseDto.updatedAt( post.getUpdatedAt() );
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

        return postsResponseDto.build();
    }

    @Override
    public PostResponseDto toPostResponseDto(Post post, Long numPostLike, Long numPostFavorite, Boolean updatable, Boolean deletable) {
        if ( post == null && numPostLike == null && numPostFavorite == null && updatable == null && deletable == null ) {
            return null;
        }

        PostResponseDtoBuilder postResponseDto = PostResponseDto.builder();

        if ( post != null ) {
            postResponseDto.writerName( postWriterName( post ) );
            postResponseDto.writerAdmissionYear( postWriterAdmissionYear( post ) );
            postResponseDto.boardName( postBoardName( post ) );
            postResponseDto.fileUrlList( mapUuidFileListToFileUrlList( post.getPostAttachImageUuidFileList() ) );
            postResponseDto.isAnonymous( post.getIsAnonymous() );
            postResponseDto.isQuestion( post.getIsQuestion() );
            postResponseDto.id( post.getId() );
            postResponseDto.title( post.getTitle() );
            postResponseDto.content( post.getContent() );
            postResponseDto.isDeleted( post.getIsDeleted() );
            postResponseDto.createdAt( post.getCreatedAt() );
            postResponseDto.updatedAt( post.getUpdatedAt() );
        }
        if ( numPostLike != null ) {
            postResponseDto.numLike( numPostLike );
        }
        if ( numPostFavorite != null ) {
            postResponseDto.numFavorite( numPostFavorite );
        }
        if ( updatable != null ) {
            postResponseDto.updatable( updatable );
        }
        if ( deletable != null ) {
            postResponseDto.deletable( deletable );
        }

        return postResponseDto.build();
    }

    @Override
    public PostResponseDto toPostResponseDtoExtended(Post post, Page<CommentResponseDto> commentList, Long numComment, Long numPostLike, Long numPostFavorite, Boolean updatable, Boolean deletable) {
        if ( post == null && commentList == null && numComment == null && numPostLike == null && numPostFavorite == null && updatable == null && deletable == null ) {
            return null;
        }

        PostResponseDtoBuilder postResponseDto = PostResponseDto.builder();

        if ( post != null ) {
            postResponseDto.writerName( postWriterName( post ) );
            postResponseDto.writerAdmissionYear( postWriterAdmissionYear( post ) );
            postResponseDto.boardName( postBoardName( post ) );
            postResponseDto.fileUrlList( mapUuidFileListToFileUrlList( post.getPostAttachImageUuidFileList() ) );
            postResponseDto.content( post.getContent() );
            postResponseDto.isAnonymous( post.getIsAnonymous() );
            postResponseDto.isQuestion( post.getIsQuestion() );
            postResponseDto.id( post.getId() );
            postResponseDto.title( post.getTitle() );
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
        if ( updatable != null ) {
            postResponseDto.updatable( updatable );
        }
        if ( deletable != null ) {
            postResponseDto.deletable( deletable );
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
    public BoardPostsResponseDto toBoardPostsResponseDto(Board board, Set<Role> userRole, Boolean writable, Boolean isFavorite, Page<PostsResponseDto> post) {
        if ( board == null && userRole == null && writable == null && isFavorite == null && post == null ) {
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
        if ( post != null ) {
            boardPostsResponseDto.post( post );
        }

        return boardPostsResponseDto.build();
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
}
