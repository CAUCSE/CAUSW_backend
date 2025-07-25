package net.causw.app.main.domain.model.entity.semester;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.semester.SemesterType;

import java.time.LocalDate;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_semester")
public class Semester extends BaseEntity {

    @Column(name = "semester_year", nullable = false)
    private Integer semesterYear;

    @Column(name = "semester_type", nullable = false)
    private SemesterType semesterType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "update_user_id", nullable = false)
    private User updateUser;

    public void updateIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public static Semester of(
            Integer semesterYear,
            SemesterType semesterType,
            User updateUser
    ) {
        LocalDate startDate, endDate;
        if (semesterType == SemesterType.FIRST) {
            startDate = LocalDate.of(semesterYear, 3, 1);
            endDate = LocalDate.of(semesterYear, 8, 31);
        } else if (semesterType == SemesterType.SECOND) {
            startDate = LocalDate.of(semesterYear, 9, 1);
            endDate = LocalDate.of(semesterYear + 1, 2, 28);
        } else if (semesterType == SemesterType.SUMMER) {
            startDate = LocalDate.of(semesterYear, 7, 1);
            endDate = LocalDate.of(semesterYear, 8, 31);
        } else {
            startDate = LocalDate.of(semesterYear, 1, 1);
            endDate = LocalDate.of(semesterYear, 2, 28);
        }
        return Semester.builder()
                .semesterYear(semesterYear)
                .semesterType(semesterType)
                .startDate(startDate)
                .endDate(endDate)
                .isCurrent(true)
                .updateUser(updateUser)
                .build();
    }
}
