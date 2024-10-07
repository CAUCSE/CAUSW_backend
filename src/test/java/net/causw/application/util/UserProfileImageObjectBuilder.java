package net.causw.application.util;

import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.adapter.persistence.uuidFile.joinEntity.UserProfileImage;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class UserProfileImageObjectBuilder extends UserProfileImage {

    public static UserProfileImage buildUserProfileImage(
            String id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            User user,
            UuidFile uuidFile
    ) {
        UserProfileImage userProfileImage = UserProfileImage.builder()
                .user(user)
                .uuidFile(uuidFile)
                .build();

        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userProfileImage, id);

            Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(userProfileImage, createdAt);

            Field updatedAtField = BaseEntity.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(userProfileImage, updatedAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return userProfileImage;
    }

    public static UserProfileImage buildUserProfileImageReduced(
            String id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UuidFile uuidFile
    ) {
        UserProfileImage userProfileImage = UserProfileImage.builder()
                .uuidFile(uuidFile)
                .build();

        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userProfileImage, id);

            Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(userProfileImage, createdAt);

            Field updatedAtField = BaseEntity.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(userProfileImage, updatedAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return userProfileImage;
    }

    public static void setUserProfileImageUser(UserProfileImage userProfileImage, User user) {
        try {
            Field userField = UserProfileImage.class.getDeclaredField("user");
            userField.setAccessible(true);
            userField.set(userProfileImage, user);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
