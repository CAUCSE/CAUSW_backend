package net.causw.adapter.persistence.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.FormType;
import net.causw.domain.model.enums.RegisteredSemester;
import net.causw.domain.model.enums.RegisteredSemesterManager;
import org.hibernate.annotations.ColumnDefault;

import java.util.*;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_form")
public class Form extends BaseEntity {

    @Column(name = "form_type", nullable = false)
    private FormType formType;

    @Column(name = "title", nullable = false)
    private String title;

    @OneToMany(mappedBy = "form", cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, orphanRemoval = true)
    @JoinColumn(nullable = false)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "circle_id", nullable = true)
    private Circle circle;

    @Column(name = "is_allowed_enrolled", nullable = false)
    private Boolean isAllowedEnrolled;

    @Column(name = "is_allowed_registered", nullable = true)
    private String EnrolledRegisteredSemester;

    @Column(name = "is_need_council_fee_paid", nullable = false)
    private Boolean isNeedCouncilFeePaid;

    @Column(name = "is_need_council_fee_paid", nullable = false)
    private Boolean isAllowedLeaveOfAbsence;

    @Column(name = "is_allowed_leave_of_absence", nullable = true)
    private String LeaveOfAbsenceRegisteredSemester;

    @Column(name = "is_allowed_graduation", nullable = false)
    private Boolean isAllowedGraduation;

    public EnumSet<RegisteredSemester> getEnrolledRegisteredSemester() {
        RegisteredSemesterManager registeredSemesterManager = new RegisteredSemesterManager();
        registeredSemesterManager.deserialize(this.EnrolledRegisteredSemester);
        return registeredSemesterManager.getRegisteredSemesterEnumSet();
    }

    public EnumSet<RegisteredSemester> getLeaveOfAbsenceRegisteredSemester() {
        RegisteredSemesterManager registeredSemesterManager = new RegisteredSemesterManager();
        registeredSemesterManager.deserialize(this.LeaveOfAbsenceRegisteredSemester);
        return registeredSemesterManager.getRegisteredSemesterEnumSet();
    }

    public static Form createPostForm(
            String title,
            List<Question> questionList,
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
                .questions(questionList)
                .isAllowedEnrolled(isAllowedEnrolled)
                .EnrolledRegisteredSemester(
                        isAllowedEnrolled ?
                                enrolledRegisteredSemester.serialize()
                                : null)
                .isNeedCouncilFeePaid(isNeedCouncilFeePaid)
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
            List<Question> questionList,
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
                .questions(questionList)
                .circle(circle)
                .isAllowedEnrolled(isAllowedEnrolled)
                .EnrolledRegisteredSemester(
                        isAllowedEnrolled ?
                                enrolledRegisteredSemester.serialize()
                                : null)
                .isNeedCouncilFeePaid(isNeedCouncilFeePaid)
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
            List<Question> questionList,
            Boolean isAllowedEnrolled,
            RegisteredSemesterManager enrolledRegisteredSemester,
            Boolean isNeedCouncilFeePaid,
            Boolean isAllowedLeaveOfAbsence,
            RegisteredSemesterManager leaveOfAbsenceRegisteredSemester,
            Boolean isAllowedGraduation
    ) {
        this.title = title;
        this.questions = questionList;
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
