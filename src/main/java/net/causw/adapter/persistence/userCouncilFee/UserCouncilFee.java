package net.causw.adapter.persistence.userCouncilFee;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_user_council_fee")
public class UserCouncilFee extends BaseEntity {

    @Column(name = "is_joined_service", nullable = false)
    private Boolean isJoinedService;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = true)
    private User user;

    @OneToOne
    @JoinColumn(name = "council_fee_fake_fee", unique = true, nullable = true)
    private CouncilFeeFakeUser councilFeeFakeUser;

    @Column(name = "is_paid", nullable = false)
    private Integer paidAt;

    @Column(name = "num_of_paid_semester", nullable = false)
    private Integer numOfPaidSemester;

    @Column(name = "is_refunded", nullable = false)
    private Boolean isRefunded;

    @Column(name = "refunded_at", nullable = true)
    private Integer refundedAt;
}
