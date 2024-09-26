package net.causw.adapter.persistence.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.domain.model.enums.form.FormType;
import net.causw.domain.model.enums.form.RegisteredSemester;
import net.causw.domain.model.enums.form.RegisteredSemesterManager;

import java.util.*;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_form",
        indexes = {
                @Index(name = "circle_id_index", columnList = "circle_id")
})
public class Form extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "form_type", nullable = false)
    private FormType formType;

    @Column(name = "title", nullable = false)
    private String title;

    @OneToMany(mappedBy = "form", fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, orphanRemoval = true)
    @Builder.Default
    private List<FormQuestion> formQuestionList = new ArrayList<>();

    @Setter
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Setter
    @Column(name = "is_closed", nullable = false)
    @Builder.Default
    private Boolean isClosed = false;

    @ManyToOne
    @JoinColumn(name = "circle_id", nullable = true)
    private Circle circle;

    @Column(name = "is_allowed_enrolled", nullable = false)
    private Boolean isAllowedEnrolled;

    @Column(name = "enrolled_registered_semester", nullable = true)
    private String EnrolledRegisteredSemester;

    @Column(name = "is_need_council_fee_paid", nullable = false)
    @Builder.Default
    private Boolean isNeedCouncilFeePaid = false;

    @Column(name = "is_allowed_leave_of_absence", nullable = false)
    private Boolean isAllowedLeaveOfAbsence;

    @Column(name = "leave_of_absence_registered_semester", nullable = true)
    private String LeaveOfAbsenceRegisteredSemester;

    @Column(name = "is_allowed_graduation", nullable = false)
    private Boolean isAllowedGraduation;

    @OneToMany(mappedBy = "form", fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE }, orphanRemoval = true)
    @Builder.Default
    private List<Reply> replyList = new ArrayList<>();

    public EnumSet<RegisteredSemester> getEnrolledRegisteredSemester() {
        RegisteredSemesterManager registeredSemesterManager = RegisteredSemesterManager.fromString(this.EnrolledRegisteredSemester);
        return registeredSemesterManager.getRegisteredSemesterEnumSet();
    }

    public EnumSet<RegisteredSemester> getLeaveOfAbsenceRegisteredSemester() {
        RegisteredSemesterManager registeredSemesterManager = RegisteredSemesterManager.fromString(this.EnrolledRegisteredSemester);
        return registeredSemesterManager.getRegisteredSemesterEnumSet();
    }

    public static Form createPostForm(
            String title,
            List<FormQuestion> formQuestionList,
            Boolean isAllowedEnrolled,
            RegisteredSemesterManager enrolledRegisteredSemester,
            Boolean isNeedCouncilFeePaid,
            Boolean isAllowedLeaveOfAbsence,
            RegisteredSemesterManager leaveOfAbsenceRegisteredSemester,
            Boolean isAllowedGraduation
    ) {
        return Form.builder()
                .formType(FormType.POST_FORM)
                .title(title)
                .formQuestionList(formQuestionList)
                .isAllowedEnrolled(isAllowedEnrolled)
                .EnrolledRegisteredSemester(
                        isAllowedEnrolled ?
                                enrolledRegisteredSemester.serialize()
                                : null)
                .isNeedCouncilFeePaid(
                        isAllowedEnrolled ?
                                isNeedCouncilFeePaid
                                : false)
                .isAllowedLeaveOfAbsence(isAllowedLeaveOfAbsence)
                .LeaveOfAbsenceRegisteredSemester(
                        isAllowedLeaveOfAbsence ?
                                leaveOfAbsenceRegisteredSemester.serialize()
                                : null)
                .isAllowedGraduation(isAllowedGraduation)
                .build();
    }

    public static Form createCircleApplicationForm(
            String title,
            List<FormQuestion> formQuestionList,
            Circle circle,
            Boolean isAllowedEnrolled,
            RegisteredSemesterManager enrolledRegisteredSemester,
            Boolean isNeedCouncilFeePaid,
            Boolean isAllowedLeaveOfAbsence,
            RegisteredSemesterManager leaveOfAbsenceRegisteredSemester,
            Boolean isAllowedGraduation
    ) {
        return Form.builder()
                .formType(FormType.CIRCLE_APPLICATION_FORM)
                .title(title)
                .formQuestionList(formQuestionList)
                .circle(circle)
                .isAllowedEnrolled(isAllowedEnrolled)
                .EnrolledRegisteredSemester(
                        isAllowedEnrolled ?
                                enrolledRegisteredSemester.serialize()
                                : null)
                .isNeedCouncilFeePaid(
                        isAllowedEnrolled ?
                                isNeedCouncilFeePaid
                                : false)
                .isAllowedLeaveOfAbsence(isAllowedLeaveOfAbsence)
                .LeaveOfAbsenceRegisteredSemester(
                        isAllowedLeaveOfAbsence ?
                                leaveOfAbsenceRegisteredSemester.serialize()
                                : null)
                .isAllowedGraduation(isAllowedGraduation)
                .build();
    }

    public void update(
            String title,
            List<FormQuestion> formQuestionList,
            Boolean isAllowedEnrolled,
            RegisteredSemesterManager enrolledRegisteredSemester,
            Boolean isNeedCouncilFeePaid,
            Boolean isAllowedLeaveOfAbsence,
            RegisteredSemesterManager leaveOfAbsenceRegisteredSemester,
            Boolean isAllowedGraduation
    ) {
        this.title = title;
        this.formQuestionList = formQuestionList;
        this.isAllowedEnrolled = isAllowedEnrolled;
        this.EnrolledRegisteredSemester = isAllowedEnrolled ?
                enrolledRegisteredSemester.serialize()
                : null;
        this.isNeedCouncilFeePaid = isNeedCouncilFeePaid;
        this.isAllowedLeaveOfAbsence = isAllowedLeaveOfAbsence;
        this.LeaveOfAbsenceRegisteredSemester = isAllowedLeaveOfAbsence ?
                leaveOfAbsenceRegisteredSemester.serialize()
                : null;
        this.isAllowedGraduation = isAllowedGraduation;
    }

}
