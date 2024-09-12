package net.causw.adapter.persistence.calendar;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_calendar")
public class Calendar extends BaseEntity {
    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "calendar_id", nullable = false)
    private UuidFile uuidFile;

    public static Calendar of(
            Integer year,
            Integer month,
            UuidFile uuidFile
    ) {
        return new Calendar(year, month, uuidFile);
    }

    public void update(Integer year, Integer month, UuidFile uuidFile) {
        this.year = year;
        this.month = month;
        this.uuidFile = uuidFile;
    }
}
