package net.causw.application.dto.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.board.BoardOfCircleResponseDto;
import net.causw.application.dto.board.BoardOfCircleResponseDto.BoardOfCircleResponseDtoBuilder;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.board.BoardResponseDto.BoardResponseDtoBuilder;
import net.causw.application.dto.comment.ChildCommentResponseDto;
import net.causw.application.dto.comment.ChildCommentResponseDto.ChildCommentResponseDtoBuilder;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.comment.CommentResponseDto.CommentResponseDtoBuilder;
import net.causw.application.dto.post.BoardPostsResponseDto;
import net.causw.application.dto.post.BoardPostsResponseDto.BoardPostsResponseDtoBuilder;
import net.causw.application.dto.post.PostResponseDto;
import net.causw.application.dto.post.PostResponseDto.PostResponseDtoBuilder;
import net.causw.application.dto.post.PostsResponseDto;
import net.causw.application.dto.post.PostsResponseDto.PostsResponseDtoBuilder;
import net.causw.domain.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-08-17T21:57:41+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.10 (Oracle Corporation)"
)
@Component
public class DtoMapperImpl implements DtoMapper {

    @Override
    public PostsResponseDto toPostsResponseDto(Post entity, Long numComment) {
        if ( entity == null && numComment == null ) {
            return null;
        }

        PostsResponseDtoBuilder postsResponseDto = PostsResponseDto.builder();

        if ( entity != null ) {
            postsResponseDto.writerName( entityWriterName( entity ) );
            postsResponseDto.writerAdmissionYear( entityWriterAdmissionYear( entity ) );
            postsResponseDto.isAnonymous( entity.getIsAnonymous() );
            postsResponseDto.isQuestion( entity.getIsQuestion() );
            postsResponseDto.id( entity.getId() );
            postsResponseDto.title( entity.getTitle() );
            postsResponseDto.createdAt( entity.getCreatedAt() );
            postsResponseDto.updatedAt( entity.getUpdatedAt() );
            postsResponseDto.isDeleted( entity.getIsDeleted() );
        }
        if ( numComment != null ) {
            postsResponseDto.numComment( numComment );
        }

        return postsResponseDto.build();
    }

    @Override
    public PostResponseDto toPostResponseDto(Post entity, Boolean updatable, Boolean deletable) {
        if ( entity == null && updatable == null && deletable == null ) {
            return null;
        }

        PostResponseDtoBuilder postResponseDto = PostResponseDto.builder();

        if ( entity != null ) {
            postResponseDto.writerName( entityWriterName( entity ) );
            postResponseDto.writerAdmissionYear( entityWriterAdmissionYear( entity ) );
            postResponseDto.writerProfileImage( entityWriterProfileImage( entity ) );
            postResponseDto.boardName( entityBoardName( entity ) );
            postResponseDto.attachmentList( attachmentsToStringList( entity.getAttachments() ) );
            postResponseDto.isAnonymous( entity.getIsAnonymous() );
            postResponseDto.isQuestion( entity.getIsQuestion() );
            postResponseDto.id( entity.getId() );
            postResponseDto.title( entity.getTitle() );
            postResponseDto.content( entity.getContent() );
            postResponseDto.isDeleted( entity.getIsDeleted() );
            postResponseDto.createdAt( entity.getCreatedAt() );
            postResponseDto.updatedAt( entity.getUpdatedAt() );
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
    public PostResponseDto toPostResponseDtoExtended(Post entity, Page<CommentResponseDto> commentList, Long numComment, Boolean updatable, Boolean deletable) {
        if ( entity == null && commentList == null && numComment == null && updatable == null && deletable == null ) {
            return null;
        }

        PostResponseDtoBuilder postResponseDto = PostResponseDto.builder();

        if ( entity != null ) {
            postResponseDto.writerName( entityWriterName( entity ) );
            postResponseDto.writerAdmissionYear( entityWriterAdmissionYear( entity ) );
            postResponseDto.writerProfileImage( entityWriterProfileImage( entity ) );
            postResponseDto.boardName( entityBoardName( entity ) );
            postResponseDto.attachmentList( attachmentsToStringList( entity.getAttachments() ) );
            postResponseDto.content( entity.getContent() );
            postResponseDto.isAnonymous( entity.getIsAnonymous() );
            postResponseDto.isQuestion( entity.getIsQuestion() );
            postResponseDto.id( entity.getId() );
            postResponseDto.title( entity.getTitle() );
            postResponseDto.isDeleted( entity.getIsDeleted() );
            postResponseDto.createdAt( entity.getCreatedAt() );
            postResponseDto.updatedAt( entity.getUpdatedAt() );
        }
        if ( commentList != null ) {
            postResponseDto.commentList( commentList );
        }
        if ( numComment != null ) {
            postResponseDto.numComment( numComment );
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
    public CommentResponseDto toCommentResponseDto(Comment entity, Long numChildComment, List<ChildCommentResponseDto> childCommentList, Boolean updatable, Boolean deletable) {
        if ( entity == null && numChildComment == null && childCommentList == null && updatable == null && deletable == null ) {
            return null;
        }

        CommentResponseDtoBuilder commentResponseDto = CommentResponseDto.builder();

        if ( entity != null ) {
            commentResponseDto.writerName( entityWriterName1( entity ) );
            commentResponseDto.writerAdmissionYear( entityWriterAdmissionYear1( entity ) );
            commentResponseDto.writerProfileImage( entityWriterProfileImage1( entity ) );
            commentResponseDto.postId( entityPostId( entity ) );
            commentResponseDto.isAnonymous( entity.getIsAnonymous() );
            commentResponseDto.id( entity.getId() );
            commentResponseDto.content( entity.getContent() );
            commentResponseDto.createdAt( entity.getCreatedAt() );
            commentResponseDto.updatedAt( entity.getUpdatedAt() );
            commentResponseDto.isDeleted( entity.getIsDeleted() );
            commentResponseDto.childCommentList( childCommentListToChildCommentResponseDtoList( entity.getChildCommentList() ) );
        }
        if ( numChildComment != null ) {
            commentResponseDto.numChildComment( numChildComment );
        }
        if ( updatable != null ) {
            commentResponseDto.updatable( updatable );
        }
        if ( deletable != null ) {
            commentResponseDto.deletable( deletable );
        }

        return commentResponseDto.build();
    }

    @Override
    public ChildCommentResponseDto toChildCommentResponseDto(ChildComment entity, Long numChildCommentLike, Boolean updatable, Boolean deletable) {
        if ( entity == null && numChildCommentLike == null && updatable == null && deletable == null ) {
            return null;
        }

        ChildCommentResponseDtoBuilder childCommentResponseDto = ChildCommentResponseDto.builder();

        if ( entity != null ) {
            childCommentResponseDto.writerName( entityWriterName2( entity ) );
            childCommentResponseDto.writerAdmissionYear( entityWriterAdmissionYear2( entity ) );
            childCommentResponseDto.writerProfileImage( entityWriterProfileImage2( entity ) );
            childCommentResponseDto.isAnonymous( entity.getIsAnonymous() );
            childCommentResponseDto.id( entity.getId() );
            childCommentResponseDto.content( entity.getContent() );
            childCommentResponseDto.createdAt( entity.getCreatedAt() );
            childCommentResponseDto.updatedAt( entity.getUpdatedAt() );
            childCommentResponseDto.isDeleted( entity.getIsDeleted() );
            childCommentResponseDto.tagUserName( entity.getTagUserName() );
            childCommentResponseDto.refChildComment( entity.getRefChildComment() );
        }
        if ( numChildCommentLike != null ) {
            childCommentResponseDto.numLike( numChildCommentLike );
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
    public BoardPostsResponseDto toBoardPostsResponseDto(Board entity, Set<Role> userRole, Boolean writable, Boolean isFavorite, Page<PostsResponseDto> post) {
        if ( entity == null && userRole == null && writable == null && isFavorite == null && post == null ) {
            return null;
        }

        BoardPostsResponseDtoBuilder boardPostsResponseDto = BoardPostsResponseDto.builder();

        if ( entity != null ) {
            boardPostsResponseDto.boardId( entity.getId() );
            boardPostsResponseDto.boardName( entity.getName() );
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

    @Override
    public BoardResponseDto toBoardResponseDto(Board entity, List<String> createRoleList, Boolean writable, String circleId, String circleName) {
        if ( entity == null && createRoleList == null && writable == null && circleId == null && circleName == null ) {
            return null;
        }

        BoardResponseDtoBuilder boardResponseDto = BoardResponseDto.builder();

        if ( entity != null ) {
            boardResponseDto.id( entity.getId() );
            boardResponseDto.name( entity.getName() );
            boardResponseDto.description( entity.getDescription() );
            boardResponseDto.category( entity.getCategory() );
            boardResponseDto.isDeleted( entity.getIsDeleted() );
        }
        if ( createRoleList != null ) {
            List<String> list = createRoleList;
            if ( list != null ) {
                boardResponseDto.createRoleList( new ArrayList<String>( list ) );
            }
        }
        if ( writable != null ) {
            boardResponseDto.writable( writable );
        }
        if ( circleId != null ) {
            boardResponseDto.circleId( circleId );
        }
        if ( circleName != null ) {
            boardResponseDto.circleName( circleName );
        }

        return boardResponseDto.build();
    }

    @Override
    public BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board board, Post post, Long numComment, boolean writable) {
        if ( board == null && post == null && numComment == null ) {
            return null;
        }

        BoardOfCircleResponseDtoBuilder boardOfCircleResponseDto = BoardOfCircleResponseDto.builder();

        if ( board != null ) {
            boardOfCircleResponseDto.id( board.getId() );
            boardOfCircleResponseDto.name( board.getName() );
            boardOfCircleResponseDto.isDeleted( board.getIsDeleted() );
        }
        if ( post != null ) {
            boardOfCircleResponseDto.postId( post.getId() );
            boardOfCircleResponseDto.postTitle( post.getTitle() );
            boardOfCircleResponseDto.postWriterName( entityWriterName( post ) );
            boardOfCircleResponseDto.postWriterStudentId( postWriterStudentId( post ) );
            boardOfCircleResponseDto.postCreatedAt( post.getCreatedAt() );
        }
        if ( numComment != null ) {
            boardOfCircleResponseDto.postNumComment( numComment );
        }
        boardOfCircleResponseDto.writable( writable );

        return boardOfCircleResponseDto.build();
    }

    @Override
    public BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board entity, Long numComment, boolean writable) {
        if ( entity == null && numComment == null ) {
            return null;
        }

        BoardOfCircleResponseDtoBuilder boardOfCircleResponseDto = BoardOfCircleResponseDto.builder();

        if ( entity != null ) {
            boardOfCircleResponseDto.id( entity.getId() );
            boardOfCircleResponseDto.name( entity.getName() );
            boardOfCircleResponseDto.isDeleted( entity.getIsDeleted() );
        }
        if ( numComment != null ) {
            boardOfCircleResponseDto.postNumComment( numComment );
        }
        boardOfCircleResponseDto.writable( writable );

        return boardOfCircleResponseDto.build();
    }

    private String entityWriterName(Post post) {
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

    private Integer entityWriterAdmissionYear(Post post) {
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

    private String entityWriterProfileImage(Post post) {
        if ( post == null ) {
            return null;
        }
        User writer = post.getWriter();
        if ( writer == null ) {
            return null;
        }
        String profileImage = writer.getProfileImage();
        if ( profileImage == null ) {
            return null;
        }
        return profileImage;
    }

    private String entityBoardName(Post post) {
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

    private String entityWriterName1(Comment comment) {
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

    private Integer entityWriterAdmissionYear1(Comment comment) {
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

    private String entityWriterProfileImage1(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        User writer = comment.getWriter();
        if ( writer == null ) {
            return null;
        }
        String profileImage = writer.getProfileImage();
        if ( profileImage == null ) {
            return null;
        }
        return profileImage;
    }

    private String entityPostId(Comment comment) {
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

    protected ChildCommentResponseDto childCommentToChildCommentResponseDto(ChildComment childComment) {
        if ( childComment == null ) {
            return null;
        }

        ChildCommentResponseDtoBuilder childCommentResponseDto = ChildCommentResponseDto.builder();

        childCommentResponseDto.id( childComment.getId() );
        childCommentResponseDto.content( childComment.getContent() );
        childCommentResponseDto.createdAt( childComment.getCreatedAt() );
        childCommentResponseDto.updatedAt( childComment.getUpdatedAt() );
        childCommentResponseDto.isDeleted( childComment.getIsDeleted() );
        childCommentResponseDto.tagUserName( childComment.getTagUserName() );
        childCommentResponseDto.refChildComment( childComment.getRefChildComment() );
        childCommentResponseDto.isAnonymous( childComment.getIsAnonymous() );

        return childCommentResponseDto.build();
    }

    protected List<ChildCommentResponseDto> childCommentListToChildCommentResponseDtoList(List<ChildComment> list) {
        if ( list == null ) {
            return null;
        }

        List<ChildCommentResponseDto> list1 = new ArrayList<ChildCommentResponseDto>( list.size() );
        for ( ChildComment childComment : list ) {
            list1.add( childCommentToChildCommentResponseDto( childComment ) );
        }

        return list1;
    }

    private String entityWriterName2(ChildComment childComment) {
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

    private Integer entityWriterAdmissionYear2(ChildComment childComment) {
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

    private String entityWriterProfileImage2(ChildComment childComment) {
        if ( childComment == null ) {
            return null;
        }
        User writer = childComment.getWriter();
        if ( writer == null ) {
            return null;
        }
        String profileImage = writer.getProfileImage();
        if ( profileImage == null ) {
            return null;
        }
        return profileImage;
    }

    private String postWriterStudentId(Post post) {
        if ( post == null ) {
            return null;
        }
        User writer = post.getWriter();
        if ( writer == null ) {
            return null;
        }
        String studentId = writer.getStudentId();
        if ( studentId == null ) {
            return null;
        }
        return studentId;
    }
}
