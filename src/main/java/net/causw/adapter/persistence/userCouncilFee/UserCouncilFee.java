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

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "council_fee_fake_user_id", unique = true, nullable = true)
    private CouncilFeeFakeUser councilFeeFakeUser;

    @Column(name = "paid_at", nullable = false)
    private Integer paidAt;

    @Column(name = "num_of_paid_semester", nullable = false)
    private Integer numOfPaidSemester;

    @Column(name = "is_refunded", nullable = false)
    private Boolean isRefunded;

    @Column(name = "refunded_at", nullable = true)
    private Integer refundedAt;

    public void update(
            Boolean isJoinedService,
            User user,
            CouncilFeeFakeUser councilFeeFakeUser,
            Integer paidAt,
            Integer numOfPaidSemester,
            Boolean isRefunded,
            Integer refundedAt
    ) {
        this.isJoinedService = isJoinedService;
        this.user = user;
        this.councilFeeFakeUser = councilFeeFakeUser;
        this.paidAt = paidAt;
        this.numOfPaidSemester = numOfPaidSemester;
        this.isRefunded = isRefunded;
        this.refundedAt = refundedAt;
    }

    public static UserCouncilFee of(
            Boolean isJoinedService,
            User user,
            CouncilFeeFakeUser councilFeeFakeUser,
            Integer paidAt,
            Integer numOfPaidSemester,
            Boolean isRefunded,
            Integer refundedAt
    ) {
        return UserCouncilFee.builder()
                .isJoinedService(isJoinedService)
                .user(user)
                .councilFeeFakeUser(councilFeeFakeUser)
                .paidAt(paidAt)
                .numOfPaidSemester(numOfPaidSemester)
                .isRefunded(isRefunded)
                .refundedAt(refundedAt)
                .build();
    }

    public Integer getCurrentCompletedSemester() {
        return user != null ?
            user.getCurrentCompletedSemester() : councilFeeFakeUser.getCurrentCompletedSemester();
    }
}
