package net.causw.app.main.dto.util.dtoMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.model.entity.semester.Semester;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.model.entity.userAcademicRecord.UserAcademicRecordLog;
import net.causw.app.main.dto.userAcademicRecordApplication.CurrentUserAcademicRecordApplicationResponseDto;
import net.causw.app.main.dto.userAcademicRecordApplication.UserAcademicRecordApplicationInfoResponseDto;
import net.causw.app.main.dto.userAcademicRecordApplication.UserAcademicRecordApplicationListResponseDto;
import net.causw.app.main.dto.userAcademicRecordApplication.UserAcademicRecordApplicationResponseDto;
import net.causw.app.main.dto.userAcademicRecordApplication.UserAcademicRecordInfoResponseDto;
import net.causw.app.main.dto.userAcademicRecordApplication.UserAcademicRecordListResponseDto;
import net.causw.app.main.dto.util.dtoMapper.custom.UuidFileToUrlDtoMapper;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@Mapping(target = "userId", source = "user.id")
@Mapping(target = "userName", source = "user.name")
@Mapping(target = "studentId", source = "user.studentId")
@interface UserCommonWriterMappings {
}

@Mapper(componentModel = "spring")
public interface UserAcademicRecordDtoMapper extends UuidFileToUrlDtoMapper {

	UserAcademicRecordDtoMapper INSTANCE = Mappers.getMapper(UserAcademicRecordDtoMapper.class);

	@UserCommonWriterMappings
	UserAcademicRecordListResponseDto toUserAcademicRecordListResponseDto(User user);

	@Mapping(target = "userId", source = "userAcademicRecordApplication.user.id")
	@Mapping(target = "userName", source = "userAcademicRecordApplication.user.name")
	@Mapping(target = "studentId", source = "userAcademicRecordApplication.user.studentId")
	@Mapping(target = "userAcademicRecordApplicationId", source = "userAcademicRecordApplication.id")
	UserAcademicRecordApplicationListResponseDto toUserAcademicRecordApplicationListResponseDto(
		UserAcademicRecordApplication userAcademicRecordApplication);

	@UserCommonWriterMappings
	@Mapping(target = "academicStatus", source = "user.academicStatus")
	@Mapping(target = "currentCompleteSemester", source = "user.currentCompletedSemester")
	@Mapping(target = "note", source = "user.academicStatusNote")
	@Mapping(target = "userAcademicRecordApplicationResponseDtoList", source = "userAcademicRecordApplicationResponseDtoList")
	UserAcademicRecordInfoResponseDto toUserAcademicRecordInfoResponseDto(User user,
		List<UserAcademicRecordApplicationResponseDto> userAcademicRecordApplicationResponseDtoList);

	@Mapping(target = "targetAcademicStatus", source = "userAcademicRecordLog.targetAcademicRecordStatus")
	@Mapping(target = "userNote", source = "userAcademicRecordLog.note")
	@Mapping(target = "attachedImageUrlList", source = "userAcademicRecordLog.userAcademicRecordLogAttachImageList", qualifiedByName = "mapUuidFileListToFileUrlList")
	@Mapping(target = "changeDate", source = "userAcademicRecordLog.updatedAt")
	UserAcademicRecordApplicationResponseDto toUserAcademicRecordApplicationResponseDto(
		UserAcademicRecordLog userAcademicRecordLog);

	@Mapping(target = "userId", source = "userAcademicRecordApplication.user.id")
	@Mapping(target = "userName", source = "userAcademicRecordApplication.user.name")
	@Mapping(target = "studentId", source = "userAcademicRecordApplication.user.studentId")
	@Mapping(target = "academicRecordRequestStatus", source = "userAcademicRecordApplication.academicRecordRequestStatus")
	@Mapping(target = "targetAcademicStatus", source = "userAcademicRecordApplication.targetAcademicStatus")
	@Mapping(target = "targetCompletedSemester", source = "userAcademicRecordApplication.targetCompletedSemester")
	@Mapping(target = "note", source = "userAcademicRecordApplication.note")
	@Mapping(target = "attachedImageUrlList", source = "userAcademicRecordApplication.userAcademicRecordAttachImageList", qualifiedByName = "mapUuidFileListToFileUrlList")
	@Mapping(target = "rejectMessage", source = "userAcademicRecordApplication.rejectMessage")
	UserAcademicRecordApplicationInfoResponseDto toUserAcademicRecordApplicationInfoResponseDto(
		UserAcademicRecordApplication userAcademicRecordApplication);

	@Mapping(target = "currentSemesterYear", source = "semester.semesterYear")
	@Mapping(target = "currentSemesterType", source = "semester.semesterType")
	@Mapping(target = "isRejected", source = "isRejected")
	@Mapping(target = "rejectMessage", source = "userAcademicRecordApplication.rejectMessage")
	@Mapping(target = "targetAcademicStatus", source = "userAcademicRecordApplication.targetAcademicStatus")
	@Mapping(target = "targetCompletedSemester", source = "userAcademicRecordApplication.targetCompletedSemester")
	@Mapping(target = "userNote", source = "userAcademicRecordApplication.note")
	@Mapping(target = "attachedImageUrlList", source = "userAcademicRecordApplication.userAcademicRecordAttachImageList", qualifiedByName = "mapUuidFileListToFileUrlList")
	CurrentUserAcademicRecordApplicationResponseDto toCurrentUserAcademicRecordResponseDto(Semester semester,
		UserAcademicRecordApplication userAcademicRecordApplication, Boolean isRejected);

}
