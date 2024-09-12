package net.causw.adapter.persistence.locker;

import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "TB_LOCKER_LOCATION")
public class LockerLocation extends BaseEntity {
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    public static LockerLocation of(String name){
        return LockerLocation.builder()
                .name(name)
                .build();
    };

    public void update(String name){
        this.name = name;
    }
}
