package net.causw.adapter.persistence.flag;

import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "TB_FLAG")
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
        return new Flag(
                key,
                value
        );
    }
}
