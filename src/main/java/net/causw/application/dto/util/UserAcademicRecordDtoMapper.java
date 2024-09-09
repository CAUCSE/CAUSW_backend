package net.causw.application.dto.util;

import net.causw.adapter.persistence.semester.Semester;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.user.UserAcademicRecordApplication;
import net.causw.adapter.persistence.user.UserAcademicRecordLog;
import net.causw.application.dto.userAcademicRecordApplication.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@interface UserAcademicRecordCommonWriterMappings {}

@Mapper(componentModel = "spring")
public interface UserAcademicRecordDtoMapper {

    UserAcademicRecordDtoMapper INSTANCE = Mappers.getMapper(UserAcademicRecordDtoMapper.class);

    @UserAcademicRecordCommonWriterMappings
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "studentId", source = "user.studentId")
    UserAcademicRecordListResponseDto toUserAcademicRecordListResponseDto(User user);

    @UserAcademicRecordCommonWriterMappings
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "studentId", source = "user.studentId")
    @Mapping(target = "academicStatus", source = "user.academicStatus")
    @Mapping(target = "currentCompleteSemester", source = "user.currentCompletedSemester")
    @Mapping(target = "note", source = "user.academicStatusNote")
    @Mapping(target = "userAcademicRecordApplicationResponseDtoList", source = "userAcademicRecordApplicationResponseDtoList")
    UserAcademicRecordInfoResponseDto toUserAcademicRecordInfoResponseDto(User user, List<UserAcademicRecordApplicationResponseDto> userAcademicRecordApplicationResponseDtoList);

    @UserAcademicRecordCommonWriterMappings
    @Mapping(target = "targetAcademicStatus", source = "userAcademicRecordLog.targetAcademicRecordStatus")
    @Mapping(target = "userNote", source = "userAcademicRecordLog.note")
    @Mapping(target = "attachedImageUrlList", source = "userAcademicRecordLog.targetUserAcademicRecordApplication.uuidFileList.stream().map(UuidFile::getUrl).toList()")
    @Mapping(target = "changeDate", source = "userAcademicRecordLog.updatedAt")
    UserAcademicRecordApplicationResponseDto toUserAcademicRecordApplicationResponseDto(UserAcademicRecordLog userAcademicRecordLog);

    @UserAcademicRecordCommonWriterMappings
    @Mapping(target = "userId", source = "userAcademicRecordApplication.user.id")
    @Mapping(target = "userName", source = "userAcademicRecordApplication.user.name")
    @Mapping(target = "studentId", source = "userAcademicRecordApplication.user.studentId")
    @Mapping(target = "academicRecordRequestStatus", source = "userAcademicRecordApplication.academicRecordRequestStatus")
    @Mapping(target = "targetAcademicStatus", source = "userAcademicRecordApplication.targetAcademicRecordStatus")
    @Mapping(target = "targetCompletedSemester", source = "userAcademicRecordApplication.targetCompletedSemester")
    @Mapping(target = "note", source = "userAcademicRecordApplication.note")
    @Mapping(target = "attachedImageUrlList", source = "userAcademicRecordApplication.uuidFileList.stream().map(UuidFile::getUrl).toList()")
    @Mapping(target = "rejectMessage", source = "userAcademicRecordApplication.rejectMessage")
    UserAcademicRecordApplicationInfoResponseDto toUserAcademicRecordApplicationInfoResponseDto(UserAcademicRecordApplication userAcademicRecordApplication);

    @UserAcademicRecordCommonWriterMappings
    @Mapping(target = "currentSemesterYear", source = "semester.semesterYear")
    @Mapping(target = "currentSemesterType", source = "semester.semesterType")
    @Mapping(target = "isRejected", source = "isRejected")
    @Mapping(target = "rejectMessage", source = "userAcademicRecordApplication.rejectMessage")
    @Mapping(target = "targetAcademicStatus", source = "userAcademicRecordApplication.targetAcademicStatus")
    @Mapping(target = "targetCompletedSemester", source = "userAcademicRecordApplication.targetCompletedSemester")
    @Mapping(target = "userNote", source = "userAcademicRecordApplication.note")
    @Mapping(target = "attachedImageUrlList", source = "userAcademicRecordApplication.uuidFileList.stream().map(UuidFile::getUrl).toList()")
    CurrentUserAcademicRecordApplicationResponseDto toCurrentUserAcademicRecordResponseDto(Semester semester, UserAcademicRecordApplication userAcademicRecordApplication, Boolean isRejected);
}
