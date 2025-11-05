package net.causw.app.main.domain.moving.dto.semester;

import net.causw.app.main.domain.moving.model.enums.semester.SemesterType;

import lombok.Getter;

@Getter
public class CreateSemesterRequestDto {

	private Integer semesterYear;

	private SemesterType semesterType;

}
