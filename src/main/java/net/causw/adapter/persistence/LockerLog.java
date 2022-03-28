package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.LockerLogAction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_LOCKER_LOG")
public class LockerLog extends BaseEntity{
    @Column(name = "locker_number", nullable = false)
    private Long lockerNumber;

    @Column(name = "locker_location_name")
    private String lockerLocationName;

    @Column(name = "user_email", nullable = true)
    private String userEmail;

    @Column(name = "user_name", nullable = true)
    private String userName;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private LockerLogAction action;

    @Column(name = "message", nullable = true)
    private String message;

    private LockerLog(
            String id,
            Long lockerNumber,
            String lockerLocationName,
            String userEmail,
            String userName,
            LockerLogAction action,
            String message
    ) {
        super(id);
        this.lockerNumber = lockerNumber;
        this.lockerLocationName = lockerLocationName;
        this.userEmail = userEmail;
        this.userName = userName;
        this.action = action;
        this.message = message;
    }

    private LockerLog(
            Long lockerNumber,
            String lockerLocationName,
            String userEmail,
            String userName,
            LockerLogAction action,
            String message
    ) {
        this.lockerNumber = lockerNumber;
        this.lockerLocationName = lockerLocationName;
        this.userEmail = userEmail;
        this.userName = userName;
        this.action = action;
        this.message = message;
    }

    public static LockerLog of(
            Long lockerNumber,
            String lockerLocationName,
            String userEmail,
            String userName,
            LockerLogAction action,
            String message
    ) {
        return new LockerLog(
                lockerNumber,
                lockerLocationName,
                userEmail,
                userName,
                action,
                message
        );
    }
}
