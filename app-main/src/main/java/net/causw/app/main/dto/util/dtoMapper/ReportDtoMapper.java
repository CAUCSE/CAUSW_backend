package net.causw.app.main.dto.util.dtoMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.report.ReportReason;
import net.causw.app.main.dto.report.ReportCreateResponseDto;
import net.causw.app.main.dto.report.ReportedCommentNativeProjection;
import net.causw.app.main.dto.report.ReportedCommentResponseDto;
import net.causw.app.main.dto.report.ReportedPostNativeProjection;
import net.causw.app.main.dto.report.ReportedPostResponseDto;
import net.causw.app.main.dto.report.ReportedUserResponseDto;
import net.causw.app.main.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;

@Mapper(componentModel = "spring")
public interface ReportDtoMapper extends UuidFileToUrlDtoMapper {

	ReportDtoMapper INSTANCE = Mappers.getMapper(ReportDtoMapper.class);

	// 신고 생성 응답
	default ReportCreateResponseDto toReportCreateResponseDto(String message) {
		return new ReportCreateResponseDto(message);
	}

	// 게시글
	@Mapping(target = "reportReasonDescription", source = "reportReason", qualifiedByName = "mapReportReasonToDescription")
	@Mapping(target = "url", source = ".", qualifiedByName = "buildPostUrl")
	ReportedPostResponseDto toReportedPostDto(ReportedPostNativeProjection projection);

	// 댓글, 대댓글
	@Mapping(target = "commentId", source = "contentId")
	@Mapping(target = "commentContent", source = "content")
	@Mapping(target = "parentPostTitle", source = "postTitle")
	@Mapping(target = "parentPostId", source = "postId")
	@Mapping(target = "reportReasonDescription", source = "reportReason", qualifiedByName = "mapReportReasonToDescription")
	@Mapping(target = "url", source = ".", qualifiedByName = "buildCommentUrl")
	ReportedCommentResponseDto toReportedCommentDto(ReportedCommentNativeProjection projection);

	// 신고된 사용자
	@Mapping(target = "userId", source = "id")
	@Mapping(target = "userName", source = "name")
	@Mapping(target = "userNickname", source = "nickname")
	@Mapping(target = "totalReportCount", source = "reportCount")
	@Mapping(target = "profileImage", source = "userProfileImage", qualifiedByName = "mapUuidFileToFileUrl")
	@Mapping(target = "userState", source = "state")
	ReportedUserResponseDto toReportedUserDto(User user);

	// Native Query용 description 변환
	@Named("mapReportReasonToDescription")
	default String mapReportReasonToDescription(String reportReason) {
		return ReportReason.valueOf(reportReason).getDescription();
	}

	// 게시글 URL 생성
	@Named("buildPostUrl")
	default String buildPostUrl(ReportedPostNativeProjection projection) {
		return "/board/" + projection.getBoardId() + "/" + projection.getPostId();
	}

	// 댓글 URL 생성
	@Named("buildCommentUrl")
	default String buildCommentUrl(ReportedCommentNativeProjection projection) {
		return "/board/" + projection.getBoardId() + "/" + projection.getPostId();
	}
}