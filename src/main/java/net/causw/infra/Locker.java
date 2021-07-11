package net.causw.infra;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_LOCKER")
public class Locker extends BaseEntity {
    @Column(name = "locker_number", unique = true)
    private Long lockerNumber;

    // Foreign Key

    @OneToOne(mappedBy = "locker")
    private User user;

    private Locker(Long lockerNumber, User user) {
        this.lockerNumber = lockerNumber;
        this.user = user;
    }

    public static Locker of(Long lockerNumber, User user) {
        return new Locker(lockerNumber, user);
    }
}
