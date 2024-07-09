package net.causw.adapter.persistence.locker;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.locker.LockerDomainModel;
import net.causw.domain.model.locker.LockerLocationDomainModel;
import net.causw.domain.model.user.UserDomainModel;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static Locker from(LockerDomainModel lockerDomainModel) {
        return new Locker(
                lockerDomainModel.getId(),
                lockerDomainModel.getLockerNumber(),
                lockerDomainModel.getIsActive(),
                lockerDomainModel.getUser().map(User::from).orElse(null),
                LockerLocation.from(lockerDomainModel.getLockerLocation())
        );
    }

    public static Locker of(
            Long lockerNumber,
            Boolean isActive,
            User user,
            LockerLocation location,
            LocalDateTime expireDate
    ) {
        return new Locker(
                lockerNumber,
                isActive,
                expireDate,
                user,
                location
        );
    }


    public Optional<User> getUser() {
        return Optional.ofNullable(this.user);
    }

    public void update(boolean isActive, User user, LocalDateTime expireDate) {
        this.isActive = isActive;
        this.user = user;
        this.expireDate = expireDate;
    }

    public void updateLocation(LockerLocation location) {
        this.location = location;
    }

    public void register(User user, LocalDateTime expiredAt) {
        this.user = user;
        this.isActive = Boolean.FALSE;
        this.expireDate = expiredAt;
    }

    public void returnLocker() {
        this.user = null;
        this.isActive = Boolean.TRUE;
        this.expireDate = null;
    }

    public void extendExpireDate(LocalDateTime expiredAt) {
        this.expireDate = expiredAt;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
        this.user = null;
    }

    public void move(LockerLocation lockerLocation) {
        this.location = lockerLocation;
    }


}
