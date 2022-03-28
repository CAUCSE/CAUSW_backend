package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.LockerDomainModel;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
