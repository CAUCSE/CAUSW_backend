package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_circle_main_image_uuid_file",
indexes = {
    @Index(name = "idx_circle_main_image_circle_id", columnList = "circle_id"),
    @Index(name = "idx_circle_main_image_uuid_file_id", columnList = "uuid_file_id")
})
public class CircleMainImage extends JoinEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circle_id", nullable = false)
    private Circle circle;

    private CircleMainImage(Circle circle, UuidFile uuidFile) {
        super(uuidFile);
        this.circle = circle;
    }

    public static CircleMainImage of(Circle circle, UuidFile uuidFile) {
        return new CircleMainImage(circle, uuidFile);
    }

    public CircleMainImage updateUuidFileAndReturnSelf(UuidFile uuidFile) {
        this.uuidFile = uuidFile;
        return this;
    }

}
