package net.causw.infra;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_LOCKER")
public class LockerLog extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "locker_type")
    private LockerType lockerType;

    // Foreign Key

    @ManyToOne
    @JoinColumn(name = "locker_id", insertable = false, updatable = false)
    private Locker locker;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    private LockerLog(LockerType lockerType, Locker locker, User user) {
        this.lockerType = lockerType;
        this.locker = locker;
        this.user = user;
    }

    public static LockerLog of(LockerType lockerType, Locker locker, User user) {
        return new LockerLog(lockerType, locker, user);
    }
}
