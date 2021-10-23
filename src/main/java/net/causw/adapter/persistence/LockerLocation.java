package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_LOCKER_LOCATION")
public class LockerLocation extends BaseEntity {
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name ="description", unique = false, nullable = true)
    private String description;

    private LockerLocation(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static LockerLocation of(String name, String description) {
        return new LockerLocation(name, description);
    }
}
