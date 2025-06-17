package net.causw.adapter.persistence.flag;

import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Getter
@Setter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_flag")
public class Flag extends BaseEntity {
    @Column(name = "tb_key", unique = true, nullable = false)
    private String key;

    @Column(name = "value")
    @ColumnDefault("false")
    private Boolean value;

    public static Flag of(
            String key,
            Boolean value
    ) {
        return Flag.builder()
                .key(key)
                .value(value)
                .build();
    }
}
