package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private LockerLocation location;

    private Locker(Long lockerNumber, Boolean isActive, User user, LockerLocation location) {
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.user = user;
        this.location = location;
    }

    public static Locker of(Long lockerNumber, Boolean isActive, User user, LockerLocation location) {
        return new Locker(lockerNumber, isActive, user, location);
    }
}
