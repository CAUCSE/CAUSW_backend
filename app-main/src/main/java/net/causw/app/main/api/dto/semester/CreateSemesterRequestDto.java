package net.causw.app.main.api.dto.semester;

import net.causw.app.main.domain.campus.semester.enums.SemesterType;

import lombok.Getter;

@Getter
public class CreateSemesterRequestDto {

	private Integer semesterYear;

	private SemesterType semesterType;

}
