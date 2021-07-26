package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_USER_AUTH_LOG")
public class UserAuth extends BaseEntity {
    @Column(name = "image", nullable = false)
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private UserAuth(String image, User user) {
        this.image = image;
        this.user = user;
    }

    public static UserAuth of(String image, User user) {
        return new UserAuth(image, user);
    }
}
