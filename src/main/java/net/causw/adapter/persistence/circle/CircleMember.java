package net.causw.adapter.persistence.circle;

import lombok.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.circle.CircleMemberStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter
@Setter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_circle_member")
public class CircleMember extends BaseEntity {
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CircleMemberStatus status;

    @ManyToOne
    @JoinColumn(name = "circle_id", nullable = false)
    private Circle circle;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static CircleMember of(CircleMemberStatus status, Circle circle, User user) {
        return CircleMember.builder()
                .status(status)
                .circle(circle)
                .user(user)
                .build();
    }
}
