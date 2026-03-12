package net.causw.app.main.domain.community.ceremony.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.CeremonyAttachImage;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyCategory;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;
import net.causw.app.main.domain.community.ceremony.enums.RelationType;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
@Builder(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_ceremony")
public class Ceremony extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "ceremony_type")
	private CeremonyType ceremonyType;

	@Enumerated(EnumType.STRING)
	@Column(name = "ceremony_category")
	private CeremonyCategory ceremonyCategory;

	@Column(name = "ceremony_custom_category")
	private String ceremonyCustomCategory;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "start_time")
	private LocalTime startTime;

	@Column(name = "end_time")
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "relation_type")
	private RelationType relationType;

	@Column(name = "family_relation")
	private String familyRelation;

	@Column(name = "alumni_relation")
	private String alumniRelation;

	@Column(name = "alumni_name")
	private String alumniName;

	@Column(name = "alumni_admission_year")
	private String alumniAdmissionYear;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "address")
	private String address;

	@Column(name = "postal_address")
	private String postalAddress;

	@Column(name = "detailed_address")
	private String detailedAddress;

	@Column(name = "contact")
	private String contact;

	@Column(name = "link")
	private String link;

	@Column(name = "is_set_all", nullable = false)
	@Builder.Default
	private boolean isSetAll = false;

	@ElementCollection
	@CollectionTable(name = "tb_ceremony_target_admission_years", joinColumns = @JoinColumn(name = "ceremony_id"))
	@Column(name = "admission_year")
	@Builder.Default
	private Set<String> targetAdmissionYears = new HashSet<>();

	@Setter(value = AccessLevel.PRIVATE)
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "ceremony")
	@Builder.Default
	private List<CeremonyAttachImage> ceremonyAttachImageList = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "ceremony_state", nullable = false)
	@Builder.Default
	private CeremonyState ceremonyState = CeremonyState.AWAIT;

	@Column(name = "note")
	private String note;

	public void approve() {
		this.ceremonyState = CeremonyState.ACCEPT;
	}

	public void reject() {
		this.ceremonyState = CeremonyState.REJECT;
	}

	public static Ceremony ofV1(
		User user,
		CeremonyCategory ceremonyCategory,
		String description,
		LocalDate startDate,
		LocalDate endDate,
		boolean isSetAll,
		List<String> targetAdmissionYears) {
		Set<String> targetYearsSet = targetAdmissionYears != null
			? new HashSet<>(targetAdmissionYears)
			: new HashSet<>();

		return Ceremony.builder()
			.user(user)
			.ceremonyCategory(ceremonyCategory)
			.startDate(startDate)
			.endDate(endDate)
			.description(description)
			.isSetAll(isSetAll)
			.targetAdmissionYears(targetYearsSet)
			.build();
	}

	public static Ceremony createWithImagesV1(
		User user,
		CeremonyCategory ceremonyCategory,
		String description,
		LocalDate startDate,
		LocalDate endDate,
		boolean isSetAll,
		List<String> targetAdmissionYears,
		List<UuidFile> ceremonyAttachImageUuidFileList) {
		Ceremony ceremony = Ceremony.ofV1(
			user,
			ceremonyCategory,
			description,
			startDate,
			endDate,
			isSetAll,
			targetAdmissionYears);
		List<CeremonyAttachImage> ceremonyAttachImageList = ceremonyAttachImageUuidFileList.stream()
			.map(uuidFile -> CeremonyAttachImage.of(ceremony, uuidFile))
			.toList();
		ceremony.updateCeremonyAttachImageList(ceremonyAttachImageList);
		return ceremony;
	}

	public static Ceremony of(
		User user,
		CeremonyType ceremonyType,
		CeremonyCategory ceremonyCategory,
		String ceremonyCustomCategory,
		LocalDate startDate,
		LocalDate endDate,
		LocalTime startTime,
		LocalTime endTime,
		RelationType relationType,
		String familyRelation,
		String alumniRelation,
		String alumniName,
		String alumniAdmissionYear,
		String description,
		String address,
		String postalAddress,
		String detailedAddress,
		String contact,
		String link,
		boolean isSetAll,
		List<String> targetAdmissionYears) {
		Set<String> targetYearsSet = targetAdmissionYears != null
			? new HashSet<>(targetAdmissionYears)
			: new HashSet<>();

		return Ceremony.builder()
			.user(user)
			.ceremonyType(ceremonyType)
			.ceremonyCategory(ceremonyCategory)
			.ceremonyCustomCategory(ceremonyCustomCategory)
			.startDate(startDate)
			.endDate(endDate)
			.startTime(startTime)
			.endTime(endTime)
			.relationType(relationType)
			.familyRelation(familyRelation)
			.alumniRelation(alumniRelation)
			.alumniName(alumniName)
			.alumniAdmissionYear(alumniAdmissionYear)
			.description(description)
			.address(address)
			.postalAddress(postalAddress)
			.detailedAddress(detailedAddress)
			.contact(contact)
			.link(link)
			.isSetAll(isSetAll)
			.targetAdmissionYears(targetYearsSet)
			.build();
	}

	public static Ceremony createWithImages(
		User user,
		CeremonyType ceremonyType,
		CeremonyCategory ceremonyCategory,
		String ceremonyCustomCategory,
		LocalDate startDate,
		LocalDate endDate,
		LocalTime startTime,
		LocalTime endTime,
		RelationType relationType,
		String familyRelation,
		String alumniRelation,
		String alumniName,
		String alumniAdmissionYear,
		String description,
		String address,
		String postalAddress,
		String detailedAddress,
		String contact,
		String link,
		boolean isSetAll,
		List<String> targetAdmissionYears,
		List<UuidFile> ceremonyAttachImageUuidFileList) {
		Ceremony ceremony = Ceremony.of(
			user,
			ceremonyType,
			ceremonyCategory,
			ceremonyCustomCategory,
			startDate,
			endDate,
			startTime,
			endTime,
			relationType,
			familyRelation,
			alumniRelation,
			alumniName,
			alumniAdmissionYear,
			description,
			address,
			postalAddress,
			detailedAddress,
			contact,
			link,
			isSetAll,
			targetAdmissionYears);
		List<CeremonyAttachImage> ceremonyAttachImageList = ceremonyAttachImageUuidFileList.stream()
			.map(uuidFile -> CeremonyAttachImage.of(ceremony, uuidFile))
			.toList();
		ceremony.updateCeremonyAttachImageList(ceremonyAttachImageList);
		return ceremony;
	}

	public void updateCeremonyState(CeremonyState ceremonyState) {
		this.ceremonyState = ceremonyState;
	}

	public void updateNote(String note) {
		this.note = note;
	}

	public void updateCeremonyAttachImageList(List<CeremonyAttachImage> ceremonyAttachImageList) {
		this.ceremonyAttachImageList = ceremonyAttachImageList;
	}
}
