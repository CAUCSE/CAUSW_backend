package net.causw.app.main.domain.community.post.api.v2.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.community.post.api.v2.dto.request.PostCreateRequest;
import net.causw.app.main.domain.community.post.api.v2.dto.request.PostListCondition;
import net.causw.app.main.domain.community.post.api.v2.dto.request.PostUpdateRequest;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostCreateResponse;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostListResponse;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostResponse;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostUpdateResponse;
import net.causw.app.main.domain.community.post.service.v2.dto.ImageCreateMeta;
import net.causw.app.main.domain.community.post.service.v2.dto.ImageUpdateMeta;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostDetailQuery;
import net.causw.app.main.domain.community.post.service.v2.dto.PostDetailResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostListQuery;
import net.causw.app.main.domain.community.post.service.v2.dto.PostListResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateResult;
import net.causw.app.main.domain.user.account.entity.user.User;

@Mapper(componentModel = "spring")
public interface PostDtoMapper {

	PostDtoMapper INSTANCE = Mappers.getMapper(PostDtoMapper.class);

	default PostCreateCommand toCommand(PostCreateRequest request, User user, List<MultipartFile> files) {
		List<ImageCreateMeta> imageMetas = null;
		if (request.images() != null) {
			imageMetas = request.images().stream()
				.map(img -> new ImageCreateMeta(img.order(), img.fileIndex(), img.isRepresentative()))
				.toList();
		}
		return new PostCreateCommand(
			request.content(),
			request.boardId(),
			request.isAnonymous(),
			user,
			files,
			imageMetas);
	}

	PostCreateResponse toResponse(PostCreateResult result);

	default PostUpdateCommand toUpdateCommand(String postId, PostUpdateRequest request, User user,
		List<MultipartFile> files) {
		List<ImageUpdateMeta> imageMetas = null;
		if (request.images() != null) {
			imageMetas = request.images().stream()
				.map(img -> new ImageUpdateMeta(
					img.order(),
					mapImageType(img.type()),
					img.url(),
					img.fileIndex(),
					img.isRepresentative()))
				.toList();
		}
		return new PostUpdateCommand(
			postId,
			request.content(),
			request.isAnonymous(),
			user,
			files,
			imageMetas);
	}

	default ImageUpdateMeta.Type mapImageType(PostUpdateRequest.ImageType type) {
		if (type == null) {
			return null;
		}
		return switch (type) {
			case EXISTING -> ImageUpdateMeta.Type.EXISTING;
			case NEW -> ImageUpdateMeta.Type.NEW;
		};
	}

	PostUpdateResponse toUpdateResponse(PostUpdateResult result);

	@Mapping(target = "viewer", source = "user")
	PostListQuery toListQuery(PostListCondition condition, User user);

	@Mapping(target = "posts", source = "posts")
	PostListResponse toListResponse(PostListResult result);

	@Mapping(target = "viewer", source = "user")
	PostDetailQuery toDetailQuery(String postId, User user);

	PostResponse toDetailResponse(PostDetailResult result);
}
