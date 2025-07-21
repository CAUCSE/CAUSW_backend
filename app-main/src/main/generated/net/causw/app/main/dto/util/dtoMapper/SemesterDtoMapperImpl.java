package net.causw.app.main.dto.util.dtoMapper;

import javax.annotation.processing.Generated;
import net.causw.app.main.domain.model.entity.semester.Semester;
import net.causw.app.main.dto.semester.CurrentSemesterResponseDto;
import net.causw.app.main.dto.semester.CurrentSemesterResponseDto.CurrentSemesterResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:46+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
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
