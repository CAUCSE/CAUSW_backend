package net.causw.app.main.domain.community.comment.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentAdminResponse;
import net.causw.app.main.domain.community.comment.service.dto.CommentAdminResult;

@Mapper(componentModel = "spring")
public interface CommentAdminMapper {
	CommentAdminResponse toResponse(CommentAdminResult result);
}
