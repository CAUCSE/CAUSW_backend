package net.causw.application.dto.semester;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.SemesterType;

import java.time.LocalDate;

@Getter
public class CreateSemesterRequestDto {

    private Integer semesterYear;

    private SemesterType semesterType;

}
