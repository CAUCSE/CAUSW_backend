package net.causw.adapter.persistence.locker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.locker.LockerDomainModel;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TB_LOCKER")
public class Locker extends BaseEntity {
    @Column(name = "locker_number", nullable = false)
    private Long lockerNumber;

    @Column(name = "is_active")
    @ColumnDefault("true")
    private Boolean isActive;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private LockerLocation location;

    private Locker(
            String id,
            Long lockerNumber,
            Boolean isActive,
            User user,
            LockerLocation location
    ) {
        super(id);
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.user = user;
        this.location = location;
    }

    private Locker(
            Long lockerNumber,
            Boolean isActive,
            User user,
            LockerLocation location
    ) {
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.user = user;
        this.location = location;
    }

    public static Locker of(
            Long lockerNumber,
            Boolean isActive,
            User user,
            LockerLocation location
    ) {
        return new Locker(
                lockerNumber,
                isActive,
                user,
                location
        );
    }

    public static Locker from(LockerDomainModel lockerDomainModel) {
        return new Locker(
                lockerDomainModel.getId(),
                lockerDomainModel.getLockerNumber(),
                lockerDomainModel.getIsActive(),
                lockerDomainModel.getUser().map(User::from).orElse(null),
                LockerLocation.from(lockerDomainModel.getLockerLocation())
        );
    }

    public Optional<User> getUser() {
        return Optional.ofNullable(this.user);
    }
}
