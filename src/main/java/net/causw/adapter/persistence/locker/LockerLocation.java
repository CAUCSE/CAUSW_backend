package net.causw.adapter.persistence.locker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.locker.LockerLocationDomainModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TB_LOCKER_LOCATION")
public class LockerLocation extends BaseEntity {
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    private LockerLocation(String id, String name) {
        super(id);
        this.name = name;
    }

    private LockerLocation(String name) {
        this.name = name;
    }

    public static LockerLocation of(String name) {
        return new LockerLocation(name);
    }

    public static LockerLocation from(LockerLocationDomainModel lockerLocationDomainModel) {
        return new LockerLocation(
                lockerLocationDomainModel.getId(),
                lockerLocationDomainModel.getName()
        );
    }
}
