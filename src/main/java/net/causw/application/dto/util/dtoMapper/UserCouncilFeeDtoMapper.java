package net.causw.application.dto.util.dtoMapper;

import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userCouncilFee.CouncilFeeFakeUser;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import net.causw.application.dto.userCouncilFee.UserCouncilFeeListResponseDto;
import net.causw.application.dto.userCouncilFee.UserCouncilFeeResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@interface UserCouncilFeeWriterMappings {}

@Mapper(componentModel = "spring")
public interface UserCouncilFeeDtoMapper {

    UserCouncilFeeDtoMapper INSTANCE = Mappers.getMapper(UserCouncilFeeDtoMapper.class);

    @Mapping(target = "userCouncilFeeId", source = "userCouncilFee.id")
    @Mapping(target = "isJoinedService", source = "userCouncilFee.isJoinedService")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "studentId", source = "user.studentId")
    UserCouncilFeeListResponseDto toUserCouncilFeeListResponseDto(UserCouncilFee userCouncilFee, User user);

    @Mapping(target = "userCouncilFeeId", source = "userCouncilFee.id")
    @Mapping(target = "isJoinedService", source = "userCouncilFee.isJoinedService")
    @Mapping(target = "councilFeeFakeUserId", source = "councilFeeFakeUser.id")
    @Mapping(target = "userName", source = "councilFeeFakeUser.name")
    @Mapping(target = "studentId", source = "councilFeeFakeUser.studentId")
    UserCouncilFeeListResponseDto toUserCouncilFeeListResponseDtoReduced(UserCouncilFee userCouncilFee, CouncilFeeFakeUser councilFeeFakeUser);

    @Mapping(target = "userCouncilFeeId", source = "userCouncilFee.id")
    @Mapping(target = "isJoinedService", source = "userCouncilFee.isJoinedService")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "studentId", source = "user.studentId")
    @Mapping(target = "admissionYear", source = "user.admissionYear")
    @Mapping(target = "major", source = "user.major")
    @Mapping(target = "academicStatus", source = "user.academicStatus")
    @Mapping(target = "currentCompletedSemester", source = "user.currentCompletedSemester")
    @Mapping(target = "graduationYear", source = "user.graduationYear")
    @Mapping(target = "graduationType", source = "user.graduationType")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "joinedAt", source = "user.createdAt")
    @Mapping(target = "paidAt", source = "userCouncilFee.paidAt")
    @Mapping(target = "numOfPaidSemester", source = "userCouncilFee.numOfPaidSemester")
    @Mapping(target = "isRefunded", source = "userCouncilFee.isRefunded")
    @Mapping(target = "refundedAt", source = "userCouncilFee.refundedAt")
    @Mapping(target = "restOfSemester", source = "restOfSemester")
    @Mapping(target = "isAppliedThisSemester", source = "isAppliedThisSemester")
    UserCouncilFeeResponseDto toUserCouncilFeeResponseDto(UserCouncilFee userCouncilFee, User user, Integer restOfSemester, Boolean isAppliedThisSemester);

    @Mapping(target = "userCouncilFeeId", source = "userCouncilFee.id")
    @Mapping(target = "isJoinedService", source = "userCouncilFee.isJoinedService")
    @Mapping(target = "councilFeeFakeUserId", source = "councilFeeFakeUser.id")
    @Mapping(target = "userName", source = "councilFeeFakeUser.name")
    @Mapping(target = "studentId", source = "councilFeeFakeUser.studentId")
    @Mapping(target = "admissionYear", source = "councilFeeFakeUser.admissionYear")
    @Mapping(target = "major", source = "councilFeeFakeUser.major")
    @Mapping(target = "academicStatus", source = "councilFeeFakeUser.academicStatus")
    @Mapping(target = "currentCompletedSemester", source = "councilFeeFakeUser.currentCompletedSemester")
    @Mapping(target = "graduationYear", source = "councilFeeFakeUser.graduationYear")
    @Mapping(target = "graduationType", source = "councilFeeFakeUser.graduationType")
    @Mapping(target = "phoneNumber", source = "councilFeeFakeUser.phoneNumber")
    @Mapping(target = "paidAt", source = "userCouncilFee.paidAt")
    @Mapping(target = "numOfPaidSemester", source = "userCouncilFee.numOfPaidSemester")
    @Mapping(target = "isRefunded", source = "userCouncilFee.isRefunded")
    @Mapping(target = "refundedAt", source = "userCouncilFee.refundedAt")
    @Mapping(target = "restOfSemester", source = "restOfSemester")
    @Mapping(target = "isAppliedThisSemester", source = "isAppliedThisSemester")
    UserCouncilFeeResponseDto toUserCouncilFeeResponseDtoReduced(UserCouncilFee userCouncilFee, CouncilFeeFakeUser councilFeeFakeUser, Integer restOfSemester, Boolean isAppliedThisSemester);

}
