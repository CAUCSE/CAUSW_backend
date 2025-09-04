package net.causw.app.main.dto.semester;

import net.causw.app.main.domain.model.enums.semester.SemesterType;

import lombok.Getter;

@Getter
public class CreateSemesterRequestDto {

	private Integer semesterYear;

	private SemesterType semesterType;

}
