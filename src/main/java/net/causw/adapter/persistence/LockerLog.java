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
    @Column(name = "locker_number")
    private Long lockerNumber;

    @Column(name = "user_email", nullable = true)
    private String userEmail;

    @Column(name = "user_name", nullable = true)
    private String userName;

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private LockerLogAction action;

    @Column(name = "message", nullable = true)
    private String message;



    private LockerLog(Long lockerNumber, String userEmail, String userName, LockerLogAction action) {
        this.lockerNumber = lockerNumber;
        this.userEmail = userEmail;
        this.userName = userName;
        this.action = action;
    }

    public static LockerLog of(Long lockerNumber, String userEmail, String userName, LockerLogAction action) {
        return new LockerLog(lockerNumber, userEmail, userName, action);
    }
}
