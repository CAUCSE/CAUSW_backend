package net.causw.application.util;

import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.adapter.persistence.uuidFile.joinEntity.CircleMainImage;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class CircleMainImageObjectBuilder extends CircleMainImage {

    public static CircleMainImage buildCircleMainImage(
            String id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Circle circle,
            UuidFile uuidFile
    ) {
        CircleMainImage circleMainImage = CircleMainImage.builder()
                .circle(circle)
                .uuidFile(uuidFile)
                .build();

        BaseEntityReflectionManager.setBaseEntityFields(
                circleMainImage,
                id,
                createdAt,
                updatedAt
        );

        return circleMainImage;
    }

    public static CircleMainImage buildCircleMainImageReduced(
            String id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UuidFile uuidFile
    ) {
        CircleMainImage circleMainImage = CircleMainImage.builder()
                .uuidFile(uuidFile)
                .build();

        BaseEntityReflectionManager.setBaseEntityFields(
                circleMainImage,
                id,
                createdAt,
                updatedAt
        );

        return circleMainImage;
    }

    public static void setCircleMainImageCircle(CircleMainImage circleMainImage, Circle circle) {
        try {
            Field circleField = CircleMainImage.class.getDeclaredField("circle");
            circleField.setAccessible(true);
            circleField.set(circleMainImage, circle);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
