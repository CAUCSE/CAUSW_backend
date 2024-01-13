package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TB_FLAG")
public class Flag extends BaseEntity {
    @Column(name = "tb_key", unique = true, nullable = false)
    private String key;

    @Column(name = "value")
    @ColumnDefault("false")
    private Boolean value;

    private Flag(
            String key,
            Boolean value
    ) {
        this.key = key;
        this.value = value;
    }

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
