package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_LOCKER")
public class Locker extends BaseEntity {
    @Column(name = "locker_number", unique = true, nullable = false)
    private Long lockerNumber;

    @Column(name = "is_active")
    @ColumnDefault("true")
    private Boolean isActive;

    @OneToOne(mappedBy = "locker")
    private User user;

    private Locker(Long lockerNumber, Boolean isActive ,User user) {
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.user = user;
    }

    public static Locker of(Long lockerNumber, Boolean isActive, User user) {
        return new Locker(lockerNumber, isActive, user);
    }
}
