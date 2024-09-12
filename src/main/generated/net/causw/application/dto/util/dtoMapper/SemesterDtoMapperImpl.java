package net.causw.application.dto.util.dtoMapper;

import javax.annotation.processing.Generated;
import net.causw.adapter.persistence.semester.Semester;
import net.causw.application.dto.semester.CurrentSemesterResponseDto;
import net.causw.application.dto.semester.CurrentSemesterResponseDto.CurrentSemesterResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-13T04:50:36+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.12 (Azul Systems, Inc.)"
)
@Component
public class SemesterDtoMapperImpl implements SemesterDtoMapper {

    @Override
    public CurrentSemesterResponseDto toCurrentSemesterResponseDto(Semester semester) {
        if ( semester == null ) {
            return null;
        }

        CurrentSemesterResponseDtoBuilder currentSemesterResponseDto = CurrentSemesterResponseDto.builder();

        currentSemesterResponseDto.currentSemesterYear( semester.getSemesterYear() );
        currentSemesterResponseDto.currentSemesterType( semester.getSemesterType() );

        return currentSemesterResponseDto.build();
    }
}
