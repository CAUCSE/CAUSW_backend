package net.causw.app.main.domain.user.account.api.v2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.community.block.service.dto.ChildCommentBlockCreateCommand;
import net.causw.app.main.domain.community.block.service.dto.CommentBlockCreateCommand;
import net.causw.app.main.domain.user.account.api.v2.dto.response.BlockResponseDto;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v2.dto.BlockCreateCommand;
import net.causw.app.main.domain.user.relation.service.v2.dto.BlockCreateResult;

@Mapper(componentModel = "spring")
public interface BlockDtoMapper {

	@Mapping(target = "blocker", source = "user")
	BlockCreateCommand toPostCommand(String postId, User user);

	@Mapping(target = "blocker", source = "user")
	CommentBlockCreateCommand toCommentCommand(String commentId, User user);

	@Mapping(target = "blocker", source = "user")
	ChildCommentBlockCreateCommand toChildCommentCommand(String childCommentId, User user);

	BlockResponseDto toResponse(BlockCreateResult result);
}
