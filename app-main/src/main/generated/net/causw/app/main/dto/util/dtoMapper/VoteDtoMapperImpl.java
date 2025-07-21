package net.causw.app.main.dto.util.dtoMapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.vote.Vote;
import net.causw.app.main.domain.model.entity.vote.VoteOption;
import net.causw.app.main.dto.user.UserResponseDto;
import net.causw.app.main.dto.vote.VoteOptionResponseDto;
import net.causw.app.main.dto.vote.VoteOptionResponseDto.VoteOptionResponseDtoBuilder;
import net.causw.app.main.dto.vote.VoteResponseDto;
import net.causw.app.main.dto.vote.VoteResponseDto.VoteResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:46+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
@Component
public class VoteDtoMapperImpl implements VoteDtoMapper {

    @Override
    public VoteResponseDto toVoteResponseDto(Vote vote, List<VoteOptionResponseDto> options, boolean isOwner, boolean isEnd, boolean hasVoted, Integer totalVoteCount, Integer totalUserCount) {
        if ( vote == null && options == null && totalVoteCount == null && totalUserCount == null ) {
            return null;
        }

        VoteResponseDtoBuilder voteResponseDto = VoteResponseDto.builder();

        if ( vote != null ) {
            voteResponseDto.voteId( vote.getId() );
            voteResponseDto.title( vote.getTitle() );
            voteResponseDto.allowAnonymous( vote.isAllowAnonymous() );
            voteResponseDto.allowMultiple( vote.isAllowMultiple() );
            voteResponseDto.postId( votePostId( vote ) );
        }
        if ( options != null ) {
            List<VoteOptionResponseDto> list = options;
            if ( list != null ) {
                voteResponseDto.options( new ArrayList<VoteOptionResponseDto>( list ) );
            }
        }
        if ( totalVoteCount != null ) {
            voteResponseDto.totalVoteCount( totalVoteCount );
        }
        if ( totalUserCount != null ) {
            voteResponseDto.totalUserCount( totalUserCount );
        }
        voteResponseDto.isOwner( isOwner );
        voteResponseDto.isEnd( isEnd );
        voteResponseDto.hasVoted( hasVoted );

        return voteResponseDto.build();
    }

    @Override
    public VoteOptionResponseDto toVoteOptionResponseDto(VoteOption voteOption, Integer voteCount, List<UserResponseDto> voteUsers) {
        if ( voteOption == null && voteCount == null && voteUsers == null ) {
            return null;
        }

        VoteOptionResponseDtoBuilder voteOptionResponseDto = VoteOptionResponseDto.builder();

        if ( voteOption != null ) {
            voteOptionResponseDto.id( voteOption.getId() );
            voteOptionResponseDto.optionName( voteOption.getOptionName() );
        }
        if ( voteCount != null ) {
            voteOptionResponseDto.voteCount( voteCount );
        }
        if ( voteUsers != null ) {
            List<UserResponseDto> list = voteUsers;
            if ( list != null ) {
                voteOptionResponseDto.voteUsers( new ArrayList<UserResponseDto>( list ) );
            }
        }

        return voteOptionResponseDto.build();
    }

    private String votePostId(Vote vote) {
        if ( vote == null ) {
            return null;
        }
        Post post = vote.getPost();
        if ( post == null ) {
            return null;
        }
        String id = post.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
