package net.causw.application.util;

import net.causw.adapter.persistence.base.BaseEntity;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class BaseEntityReflectionManager {

    public static void setBaseEntityFields(BaseEntity entity, String id, LocalDateTime createdAt, LocalDateTime updatedAt) {
        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);

            Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(entity, createdAt);

            Field updatedAtField = BaseEntity.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(entity, updatedAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
