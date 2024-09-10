package net.causw.adapter.persistence.userCouncilFee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_user_council_fee_log")
public class UserCouncilFeeLog extends BaseEntity {

    @ManyToOne
    @Column(name = "controlled_user_id", nullable = false)
    private User controlledUser;

    @ManyToOne
    @Column(name = "target_user_council_fee_id", nullable = false)
    private UserCouncilFee targetUserCouncilFee;

    @Column(name = "target_is_joined_service", nullable = false)
    private Boolean targetIsJoinedService;

    @ManyToOne
    @Column(name = "target_user_id", nullable = true)
    private User targetUser;

    @ManyToOne
    @Column(name = "target_council_fee_fake_user_id", nullable = true)
    private CouncilFeeFakeUser targetCouncilFeeFakeUser;

    @Column(name = "target_paid_at", nullable = false)
    private Integer targetPaidAt;

    @Column(name = "target_num_of_paid_semester", nullable = false)
    private Integer targetNumOfPaidSemester;

    @Column(name = "target_is_refunded", nullable = false)
    private Boolean targetIsRefunded;

    @Column(name = "target_refunded_at", nullable = true)
    private Integer targetRefundedAt;
}
