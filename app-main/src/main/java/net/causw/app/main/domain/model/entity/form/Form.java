package net.causw.app.main.domain.model.entity.form;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.enums.form.FormType;
import net.causw.app.main.domain.model.enums.form.RegisteredSemester;
import net.causw.app.main.domain.model.enums.form.RegisteredSemesterManager;
import net.causw.app.main.dto.form.request.create.FormCreateRequestDto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

	@OneToMany(mappedBy = "form", cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, orphanRemoval = true)
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

	@OneToMany(mappedBy = "form", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
	@Builder.Default
	private List<Reply> replyList = new ArrayList<>();

	public EnumSet<RegisteredSemester> getEnrolledRegisteredSemester() {
		RegisteredSemesterManager registeredSemesterManager = RegisteredSemesterManager.fromString(
			this.EnrolledRegisteredSemester);
		return registeredSemesterManager.getRegisteredSemesterEnumSet();
	}

	public EnumSet<RegisteredSemester> getLeaveOfAbsenceRegisteredSemester() {
		RegisteredSemesterManager registeredSemesterManager = RegisteredSemesterManager.fromString(
			this.EnrolledRegisteredSemester);
		return registeredSemesterManager.getRegisteredSemesterEnumSet();
	}

	public static Form createPostForm(
		FormCreateRequestDto formCreateRequestDto,
		List<FormQuestion> formQuestionList
	) {
		return Form.builder()
			.formType(FormType.POST_FORM)
			.title(formCreateRequestDto.getTitle())
			.formQuestionList(formQuestionList)
			.isAllowedEnrolled(formCreateRequestDto.getIsAllowedEnrolled())
			.EnrolledRegisteredSemester(
				formCreateRequestDto.getIsAllowedEnrolled() ?
					RegisteredSemesterManager.fromEnumList(
						formCreateRequestDto.getEnrolledRegisteredSemesterList()
					).serialize()
					: null)
			.isNeedCouncilFeePaid(
				(formCreateRequestDto.getIsAllowedEnrolled()
					&& formCreateRequestDto.getIsNeedCouncilFeePaid() != null) ?
					formCreateRequestDto.getIsNeedCouncilFeePaid()
					: false
			)
			.isAllowedLeaveOfAbsence(formCreateRequestDto.getIsAllowedLeaveOfAbsence())
			.LeaveOfAbsenceRegisteredSemester(
				(formCreateRequestDto.getIsAllowedLeaveOfAbsence() &&
					!(formCreateRequestDto.getLeaveOfAbsenceRegisteredSemesterList() == null ||
						formCreateRequestDto.getLeaveOfAbsenceRegisteredSemesterList().isEmpty())
				) ?
					RegisteredSemesterManager.fromEnumList(
						formCreateRequestDto.getLeaveOfAbsenceRegisteredSemesterList()
					).serialize()
					: null)
			.isAllowedGraduation(formCreateRequestDto.getIsAllowedGraduation())
			.build();
	}

	public static Form createCircleApplicationForm(
		FormCreateRequestDto formCreateRequestDto,
		List<FormQuestion> formQuestionList,
		Circle circle
	) {
		return Form.builder()
			.formType(FormType.CIRCLE_APPLICATION_FORM)
			.title(formCreateRequestDto.getTitle())
			.formQuestionList(formQuestionList)
			.circle(circle)
			.isAllowedEnrolled(formCreateRequestDto.getIsAllowedEnrolled())
			.EnrolledRegisteredSemester(
				formCreateRequestDto.getIsAllowedEnrolled() ?
					RegisteredSemesterManager.fromEnumList(
						formCreateRequestDto.getEnrolledRegisteredSemesterList()
					).serialize()
					: null)
			.isNeedCouncilFeePaid(
				(formCreateRequestDto.getIsAllowedEnrolled()
					&& formCreateRequestDto.getIsNeedCouncilFeePaid() != null) ?
					formCreateRequestDto.getIsNeedCouncilFeePaid()
					: false
			)
			.isAllowedLeaveOfAbsence(formCreateRequestDto.getIsAllowedLeaveOfAbsence())
			.LeaveOfAbsenceRegisteredSemester(
				(formCreateRequestDto.getIsAllowedLeaveOfAbsence() &&
					!(formCreateRequestDto.getLeaveOfAbsenceRegisteredSemesterList() == null ||
						formCreateRequestDto.getLeaveOfAbsenceRegisteredSemesterList().isEmpty())
				) ?
					RegisteredSemesterManager.fromEnumList(
						formCreateRequestDto.getLeaveOfAbsenceRegisteredSemesterList()
					).serialize()
					: null)
			.isAllowedGraduation(formCreateRequestDto.getIsAllowedGraduation())
			.build();
	}

}
