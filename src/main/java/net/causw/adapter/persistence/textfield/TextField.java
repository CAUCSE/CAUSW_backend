package net.causw.adapter.persistence.textfield;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.base.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TB_TEXT_FIELD")
public class TextField extends BaseEntity {
    @Column(name = "tb_key", unique = true, nullable = false)
    private String key;

    @Column(name = "value", nullable = false)
    private String value;

    private TextField(
            String key,
            String value
    ) {
        this.key = key;
        this.value = value;
    }

    public static TextField of(
            String key,
            String value
    ) {
        return new TextField(
                key,
                value
        );
    }
}
