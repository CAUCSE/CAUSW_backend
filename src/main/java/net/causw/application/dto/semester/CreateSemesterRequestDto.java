package net.causw.application.dto.semester;

import lombok.Getter;
import net.causw.domain.model.enums.SemesterType;

@Getter
public class CreateSemesterRequestDto {

    private Integer semesterYear;

    private SemesterType semesterType;

}
