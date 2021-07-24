package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.EnumType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_USERCIRCLE")
public class UserCircle extends BaseEntity {
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserCircleStatus status;

    @ManyToOne
    @JoinColumn(name = "circle_id", nullable = false)
    private Circle circle;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private UserCircle(
            UserCircleStatus status,
            Circle circle,
            User user
    ) {
        this.status = status;
        this.circle = circle;
        this.user = user;
    }

    public static UserCircle of(
            UserCircleStatus status,
            Circle circle,
            User user
    ) {
        return new UserCircle(
                status,
                circle,
                user
        );
    }
}
