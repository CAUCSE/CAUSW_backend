package net.causw.app.main.domain.model.entity.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_circle_main_image_uuid_file",
indexes = {
    @Index(name = "idx_circle_main_image_circle_id", columnList = "circle_id"),
    @Index(name = "idx_circle_main_image_uuid_file_id", columnList = "uuid_file_id")
})
public class CircleMainImage extends JoinEntity {

    @Getter
    @Setter(AccessLevel.PUBLIC)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
    public UuidFile uuidFile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circle_id", nullable = false)
    private Circle circle;

    public static CircleMainImage of(Circle circle, UuidFile uuidFile) {
        return CircleMainImage.builder()
                .circle(circle)
                .uuidFile(uuidFile)
                .build();
    }

}
