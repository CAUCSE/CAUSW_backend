package net.causw.application.dto.util.dtoMapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.comment.ChildCommentResponseDto;
import net.causw.application.dto.comment.ChildCommentResponseDto.ChildCommentResponseDtoBuilder;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.comment.CommentResponseDto.CommentResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-13T05:58:22+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.12 (Azul Systems, Inc.)"
)
@Component
public class CommentDtoMapperImpl implements CommentDtoMapper {

    @Override
    public CommentResponseDto toCommentResponseDto(Comment comment, Long numChildComment, Long numCommentLike, List<ChildCommentResponseDto> childCommentList, Boolean updatable, Boolean deletable) {
        if ( comment == null && numChildComment == null && numCommentLike == null && childCommentList == null && updatable == null && deletable == null ) {
            return null;
        }

        CommentResponseDtoBuilder commentResponseDto = CommentResponseDto.builder();

        if ( comment != null ) {
            commentResponseDto.writerName( commentWriterName( comment ) );
            commentResponseDto.writerAdmissionYear( commentWriterAdmissionYear( comment ) );
            commentResponseDto.writerProfileImage( mapUuidFileToFileUrl( commentWriterProfileImageUuidFile( comment ) ) );
            commentResponseDto.postId( commentPostId( comment ) );
            commentResponseDto.isAnonymous( comment.getIsAnonymous() );
            commentResponseDto.id( comment.getId() );
            commentResponseDto.content( comment.getContent() );
            commentResponseDto.createdAt( comment.getCreatedAt() );
            commentResponseDto.updatedAt( comment.getUpdatedAt() );
            commentResponseDto.isDeleted( comment.getIsDeleted() );
            commentResponseDto.childCommentList( childCommentListToChildCommentResponseDtoList( comment.getChildCommentList() ) );
        }
        if ( numChildComment != null ) {
            commentResponseDto.numChildComment( numChildComment );
        }
        if ( numCommentLike != null ) {
            commentResponseDto.numLike( numCommentLike );
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
    public ChildCommentResponseDto toChildCommentResponseDto(ChildComment childComment, Long numChildCommentLike, Boolean updatable, Boolean deletable) {
        if ( childComment == null && numChildCommentLike == null && updatable == null && deletable == null ) {
            return null;
        }

        ChildCommentResponseDtoBuilder childCommentResponseDto = ChildCommentResponseDto.builder();

        if ( childComment != null ) {
            childCommentResponseDto.writerName( childCommentWriterName( childComment ) );
            childCommentResponseDto.writerAdmissionYear( childCommentWriterAdmissionYear( childComment ) );
            childCommentResponseDto.writerProfileImage( mapUuidFileToFileUrl( childCommentWriterProfileImageUuidFile( childComment ) ) );
            childCommentResponseDto.isAnonymous( childComment.getIsAnonymous() );
            childCommentResponseDto.id( childComment.getId() );
            childCommentResponseDto.content( childComment.getContent() );
            childCommentResponseDto.createdAt( childComment.getCreatedAt() );
            childCommentResponseDto.updatedAt( childComment.getUpdatedAt() );
            childCommentResponseDto.isDeleted( childComment.getIsDeleted() );
            childCommentResponseDto.tagUserName( childComment.getTagUserName() );
            childCommentResponseDto.refChildComment( childComment.getRefChildComment() );
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

    private UuidFile commentWriterProfileImageUuidFile(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        User writer = comment.getWriter();
        if ( writer == null ) {
            return null;
        }
        UuidFile profileImageUuidFile = writer.getProfileImageUuidFile();
        if ( profileImageUuidFile == null ) {
            return null;
        }
        return profileImageUuidFile;
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

    private UuidFile childCommentWriterProfileImageUuidFile(ChildComment childComment) {
        if ( childComment == null ) {
            return null;
        }
        User writer = childComment.getWriter();
        if ( writer == null ) {
            return null;
        }
        UuidFile profileImageUuidFile = writer.getProfileImageUuidFile();
        if ( profileImageUuidFile == null ) {
            return null;
        }
        return profileImageUuidFile;
    }
}
