package net.causw.adapter.persistence.circle;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.CircleMemberStatus;

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
@NoArgsConstructor
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

    private CircleMember(
            String id,
            CircleMemberStatus status,
            Circle circle,
            User user
    ) {
        super(id);
        this.status = status;
        this.circle = circle;
        this.user = user;
    }

    private CircleMember(
            CircleMemberStatus status,
            Circle circle,
            User user
    ) {
        this.status = status;
        this.circle = circle;
        this.user = user;
    }

    public static CircleMember of(
            String id,
            CircleMemberStatus status,
            Circle circle,
            User user
    ) {
        return new CircleMember(
                id,
                status,
                circle,
                user
        );
    }

    public static CircleMember of(
            CircleMemberStatus status,
            Circle circle,
            User user
    ) {
        return new CircleMember(
                status,
                circle,
                user
        );
    }
}
