package net.causw.app.main.domain.community.block.api.v2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.community.block.api.v2.dto.response.BlockResponse;
import net.causw.app.main.domain.community.block.service.dto.BlockCreateResult;
import net.causw.app.main.domain.community.block.service.dto.ChildCommentBlockCreateCommand;
import net.causw.app.main.domain.user.account.entity.user.User;

@Mapper(componentModel = "spring")
public interface ChildCommentBlockDtoMapper {

	@Mapping(target = "blocker", source = "user")
	ChildCommentBlockCreateCommand toCommand(String targetUserId, String childCommentId, User user);

	BlockResponse toResponse(BlockCreateResult result);
}
