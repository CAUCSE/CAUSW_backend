package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_LOCKER_LOCATION")
public class LockerLocation extends BaseEntity {
    @Column(name = "location", unique = true, nullable = false)
    private String location;

    @Column(name ="location_desc", unique = false, nullable = true)
    private String locationDesc;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<Locker> lockerList;

    private LockerLocation(String location, String locationDesc) {
        this.location = location;
        this.locationDesc = locationDesc;
    }

    public static LockerLocation of(String location, String locationDesc) {
        return new LockerLocation(location, locationDesc);
    }
}
