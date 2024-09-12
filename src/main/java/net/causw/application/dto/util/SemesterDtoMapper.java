package net.causw.application.dto.util;

import net.causw.adapter.persistence.semester.Semester;
import net.causw.application.dto.semester.CurrentSemesterResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@interface SemesterCommonWriterMappings {}

@Mapper(componentModel = "spring")
public interface SemesterDtoMapper {

    SemesterDtoMapper INSTANCE = Mappers.getMapper(SemesterDtoMapper.class);

    @SemesterCommonWriterMappings
    @Mapping(target = "currentSemesterYear", source = "semester.semesterYear")
    @Mapping(target = "currentSemesterType", source = "semester.semesterType")
    CurrentSemesterResponseDto toCurrentSemesterResponseDto(Semester semester);
}
