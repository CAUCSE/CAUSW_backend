package net.causw.infra;

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
public class UserAuthLog extends BaseEntity {
    @Column(name = "image")
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private UserAuthLog(String image, User user) {
        this.image = image;
        this.user = user;
    }

    public static UserAuthLog of(String image, User user) {
        return new UserAuthLog(image, user);
    }
}
