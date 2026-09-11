package net.causw.app.main.domain.community.post.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.post.api.v2.dto.request.PostAdminListCondition;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostAdminDetailResponse;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostAdminSummaryResponse;
import net.causw.app.main.domain.community.post.service.dto.PostAdminDetailResult;
import net.causw.app.main.domain.community.post.service.dto.PostAdminListQuery;
import net.causw.app.main.domain.community.post.service.dto.PostAdminSummaryResult;

@Mapper(componentModel = "spring")
public interface PostAdminMapper {
	PostAdminListQuery toQuery(PostAdminListCondition condition);

	PostAdminSummaryResponse toResponse(PostAdminSummaryResult result);

	PostAdminDetailResponse toResponse(PostAdminDetailResult result);
}
