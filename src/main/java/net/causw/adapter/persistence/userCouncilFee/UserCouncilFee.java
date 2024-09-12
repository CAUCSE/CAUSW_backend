package net.causw.adapter.persistence.userCouncilFee;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;

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

    @Column(name = "is_paid", nullable = false)
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
        valid(isJoinedService, user, councilFeeFakeUser, paidAt, numOfPaidSemester, isRefunded, refundedAt);
        this.isJoinedService = isJoinedService;
        this.user = user;
        this.councilFeeFakeUser = councilFeeFakeUser;
        this.paidAt = paidAt;
        this.numOfPaidSemester = numOfPaidSemester;
        this.isRefunded = isRefunded;
        this.refundedAt = refundedAt;
    }

    private static void valid(
            Boolean isJoinedService,
            User user,
            CouncilFeeFakeUser councilFeeFakeUser,
            Integer paidAt,
            Integer numOfPaidSemester,
            Boolean isRefunded,
            Integer refundedAt
    ) {
        if (
                (user == null ^ councilFeeFakeUser == null) ||
                        (isJoinedService && user == null) ||
                        (!isJoinedService && councilFeeFakeUser == null) ||
                        paidAt == null ||
                        numOfPaidSemester == null ||
                        isRefunded == null ||
                        (isRefunded && refundedAt == null)
        ) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_USER_COUNCIL_FEE_INFO);
        }
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
        valid(isJoinedService, user, councilFeeFakeUser, paidAt, numOfPaidSemester, isRefunded, refundedAt);
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

}
