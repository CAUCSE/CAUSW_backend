package net.causw.app.main.domain.moving.dto.util.dtoMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import net.causw.app.main.domain.moving.dto.semester.CurrentSemesterResponseDto;
import net.causw.app.main.domain.campus.semester.entity.Semester;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@interface SemesterCommonWriterMappings {
}

@Mapper(componentModel = "spring")
public interface SemesterDtoMapper {

	SemesterDtoMapper INSTANCE = Mappers.getMapper(SemesterDtoMapper.class);

	@SemesterCommonWriterMappings
	@Mapping(target = "currentSemesterYear", source = "semester.semesterYear")
	@Mapping(target = "currentSemesterType", source = "semester.semesterType")
	CurrentSemesterResponseDto toCurrentSemesterResponseDto(Semester semester);
}
