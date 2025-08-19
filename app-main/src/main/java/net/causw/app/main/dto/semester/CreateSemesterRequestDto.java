package net.causw.app.main.dto.semester;

import lombok.Getter;
import net.causw.app.main.domain.model.enums.semester.SemesterType;

@Getter
public class CreateSemesterRequestDto {

    private Integer semesterYear;

    private SemesterType semesterType;

}
