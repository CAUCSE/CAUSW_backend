package net.causw.app.main.domain.community.post.api.v2.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.community.post.api.v2.dto.request.PostCreateRequest;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostCreateResponse;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
import net.causw.app.main.domain.user.account.entity.user.User;

@Mapper(componentModel = "spring")
public interface PostDtoMapper {

	PostDtoMapper INSTANCE = Mappers.getMapper(PostDtoMapper.class);

	@Mapping(target = "writer", source = "user")
	@Mapping(target = "images", source = "files")
	PostCreateCommand toCommand(PostCreateRequest request, User user, List<MultipartFile> files);

	PostCreateResponse toResponse(PostCreateResult result);
}
