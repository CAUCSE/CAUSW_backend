package net.causw.app.main.domain.model.entity.locker;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "TB_LOCKER_LOCATION")
public class LockerLocation extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false)
    private LockerName name;

    public static LockerLocation of(LockerName name) {
        return LockerLocation.builder()
                .name(name)
                .build();
    }

    public void update(LockerName name) {
        this.name = name;
    }
    public String getName(){
        return this.name.name();
    }
}
