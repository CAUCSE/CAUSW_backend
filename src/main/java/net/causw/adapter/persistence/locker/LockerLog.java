package net.causw.adapter.persistence.locker;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.LockerLogAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Getter
@Builder
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "TB_LOCKER_LOG")
public class LockerLog extends BaseEntity {
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
