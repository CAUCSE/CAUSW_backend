package net.causw.adapter.persistence.textfield;

import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Getter
@Setter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_text_field")
public class TextField extends BaseEntity {
    @Column(name = "tb_key", unique = true, nullable = false)
    private String key;

    @Column(name = "value", nullable = false)
    private String value;

    public static TextField of(
            String key,
            String value
    ) {
        return TextField.builder()
                .key(key)
                .value(value)
                .build();
    }
}
