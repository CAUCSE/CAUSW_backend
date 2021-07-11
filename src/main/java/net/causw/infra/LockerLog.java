package net.causw.infra;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_LOCKER")
public class LockerLog extends BaseEntity {
    @Column(name = "locker_number")
    private Long lockerNumber;

    @Column(name = "user_email")
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "locker_type")
    private LockerType lockerType;

    private LockerLog(Long lockerNumber, String userEmail, LockerType lockerType) {
        this.lockerNumber = lockerNumber;
        this.userEmail = userEmail;
        this.lockerType = lockerType;
    }

    public static LockerLog of(Long lockerNumber, String userEmail, LockerType lockerType) {
        return new LockerLog(lockerNumber, userEmail, lockerType);
    }
}
