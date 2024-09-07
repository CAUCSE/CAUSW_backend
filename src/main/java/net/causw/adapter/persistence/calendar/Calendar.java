package net.causw.adapter.persistence.calendar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.base.BaseEntity;

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

    @Column(name = "image", nullable = false)
    private String image;

    public static Calendar of(
            Integer year,
            Integer month,
            String image
    ) {
        return new Calendar(year, month, image);
    }

    public void update(Integer year, Integer month, String image) {
        this.year = year;
        this.month = month;
        this.image = image;
    }
}
