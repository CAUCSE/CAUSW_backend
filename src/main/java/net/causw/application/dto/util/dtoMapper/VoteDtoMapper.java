package net.causw.application.dto.util.dtoMapper;

import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.vote.VoteResponseDto;
import net.causw.application.dto.vote.VoteOptionResponseDto;
import net.causw.adapter.persistence.vote.Vote;
import net.causw.adapter.persistence.vote.VoteOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VoteDtoMapper {

    VoteDtoMapper INSTANCE = Mappers.getMapper(VoteDtoMapper.class);

    @Mapping(target = "voteId" , source = "vote.id")
    @Mapping(target = "title" , source = "vote.title")
    @Mapping(target = "isEnd", source = "isEnd")
    @Mapping(target = "options", source = "options")
    @Mapping(target = "allowAnonymous", source = "vote.allowAnonymous")
    @Mapping(target = "allowMultiple" , source = "vote.allowMultiple")
    @Mapping(target = "postId", source = "vote.post.id")
    @Mapping(target = "isOwner" , source = "isOwner")
    VoteResponseDto toVoteResponseDto(Vote vote , List<VoteOptionResponseDto> options, boolean isOwner, boolean isEnd, boolean hasVoted);

    @Mapping(target = "id", source = "voteOption.id")
    @Mapping(target = "optionName", source = "voteOption.optionName")
    @Mapping(target = "voteCount" , source = "voteCount")
    @Mapping(target = "voteUsers" , source = "voteUsers")
    VoteOptionResponseDto toVoteOptionResponseDto(VoteOption voteOption, int voteCount , List<UserResponseDto> voteUsers);
}
