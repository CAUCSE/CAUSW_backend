package net.causw.application.dto.util;

import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.user.UserAcademicRecordApplication;
import net.causw.adapter.persistence.user.UserAcademicRecordLog;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordApplicationInfoResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordApplicationResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordInfoResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordListResponseDto;
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
    @Mapping(target = "userId", source = "entity.id")
    @Mapping(target = "userName", source = "entity.name")
    @Mapping(target = "studentId", source = "entity.studentId")
    UserAcademicRecordListResponseDto toUserAcademicRecordListResponseDto(User entity);

    @UserAcademicRecordCommonWriterMappings
    @Mapping(target = "userId", source = "entity.id")
    @Mapping(target = "userName", source = "entity.name")
    @Mapping(target = "studentId", source = "entity.studentId")
    @Mapping(target = "academicStatus", source = "entity.academicStatus")
    @Mapping(target = "currentCompleteSemester", source = "entity.currentCompletedSemester")
    @Mapping(target = "note", source = "entity.academicStatusNote")
    @Mapping(target = "userAcademicRecordApplicationResponseDtoList", source = "userAcademicRecordApplicationResponseDtoList")
    UserAcademicRecordInfoResponseDto toUserAcademicRecordInfoResponseDto(User user, List<UserAcademicRecordApplicationResponseDto> userAcademicRecordApplicationResponseDtoList);

    @UserAcademicRecordCommonWriterMappings
    @Mapping(target = "targetAcademicStatus", source = "entity.targetAcademicRecordStatus")
    @Mapping(target = "userNote", source = "entity.userNote")
    @Mapping(target = "attachedImageUrlList", source = "entity.attachedImageUrlList")
    @Mapping(target = "changeDate", source = "entity.updatedAt")
    UserAcademicRecordApplicationResponseDto toUserAcademicRecordApplicationResponseDto(UserAcademicRecordLog entity);

    @UserAcademicRecordCommonWriterMappings
    @Mapping(target = "userId", source = "entity.user.id")
    @Mapping(target = "userName", source = "entity.user.name")
    @Mapping(target = "studentId", source = "entity.user.studentId")
    @Mapping(target = "academicRecordRequestStatus", source = "entity.academicRecordRequestStatus")
    @Mapping(target = "targetAcademicStatus", source = "entity.targetAcademicRecordStatus")
    @Mapping(target = "targetCompletedSemester", source = "entity.targetCompletedSemester")
    @Mapping(target = "note", source = "entity.note")
    @Mapping(target = "attachedImageUrlList", source = "entity.attachedImageUrlList")
    @Mapping(target = "rejectMessage", source = "entity.rejectMessage")
    UserAcademicRecordApplicationInfoResponseDto toUserAcademicRecordApplicationInfoResponseDto(UserAcademicRecordApplication entity);
}
