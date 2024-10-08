package net.causw.application.util;

import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.joinEntity.CircleMainImage;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class CircleObjectBuilder extends Circle {

    public static Circle buildCircle(
            String id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String name,
            CircleMainImage circleMainImage,
            String description,
            Boolean isDeleted,
            Integer circleTax,
            Integer recruitMembers,
            LocalDateTime recruitEndDate,
            Boolean isRecruit,
            User leader
    ) {
        Circle circle = Circle.builder()
                .name(name)
                .circleMainImage(circleMainImage)
                .description(description)
                .isDeleted(isDeleted)
                .circleTax(circleTax)
                .recruitMembers(recruitMembers)
                .recruitEndDate(recruitEndDate)
                .isRecruit(isRecruit)
                .leader(leader)
                .build();

        BaseEntityReflectionManager.setBaseEntityFields(
                circle,
                id,
                createdAt,
                updatedAt
        );

        return circle;
    }

}
