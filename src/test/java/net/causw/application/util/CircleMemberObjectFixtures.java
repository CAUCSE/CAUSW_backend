package net.causw.application.util;

import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.form.Reply;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.circle.CircleMemberStatus;

import java.time.LocalDateTime;

public class CircleMemberObjectFixtures extends CircleMember {

    public static CircleMember buildCircleMember(
            String id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            CircleMemberStatus circleMemberStatus,
            Circle circle,
            User user,
            Form appliedForm,
            Reply appliedReply
    ) {
        CircleMember circleMember = CircleMember.builder()
                .status(circleMemberStatus)
                .circle(circle)
                .user(user)
                .appliedForm(appliedForm)
                .appliedReply(appliedReply)
                .build();

        BaseEntityReflectionManager.setBaseEntityFields(
                circleMember,
                id,
                createdAt,
                updatedAt
        );

        return circleMember;
    }
}
