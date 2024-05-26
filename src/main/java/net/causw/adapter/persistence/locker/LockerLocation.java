package net.causw.adapter.persistence.locker;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.locker.LockerLocationDomainModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_LOCKER_LOCATION")
public class LockerLocation extends BaseEntity {
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    private LockerLocation(String id, String name) {
        super(id);
        this.name = name;
    }

    public static LockerLocation from(LockerLocationDomainModel lockerLocationDomainModel) {
        return new LockerLocation(
                lockerLocationDomainModel.getId(),
                lockerLocationDomainModel.getName()
        );
    }
    public static LockerLocation of(String name){
        return new LockerLocation(name);
    };

    public void update(String name){
        this.name = name;
    }
}
