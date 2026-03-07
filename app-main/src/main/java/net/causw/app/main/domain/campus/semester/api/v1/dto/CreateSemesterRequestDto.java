package net.causw.app.main.domain.campus.semester.api.v1.dto;

import net.causw.app.main.domain.campus.semester.enums.SemesterType;

import lombok.Getter;

@Getter
public class CreateSemesterRequestDto {

	private Integer semesterYear;

	private SemesterType semesterType;

}
