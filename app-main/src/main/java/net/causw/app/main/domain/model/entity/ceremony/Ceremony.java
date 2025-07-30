package net.causw.app.main.domain.model.entity.ceremony;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.CeremonyAttachImage;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;
//import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.CeremonyAttachImage;
import net.causw.app.main.domain.model.enums.ceremony.CeremonyCategory;
import net.causw.app.main.domain.model.enums.ceremony.CeremonyState;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


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
    @Column(name = "ceremony_category", nullable = false)
    private CeremonyCategory ceremonyCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "ceremony_state", nullable = false)
    @Builder.Default
    private CeremonyState ceremonyState = CeremonyState.AWAIT;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "note", nullable = true)
    private String note = "";

    @Column(name = "is_set_all", nullable = false)
    @Builder.Default
    private boolean isSetAll = false;

    @ElementCollection
    @CollectionTable(name = "tb_ceremony_target_admission_years", joinColumns = @JoinColumn(name = "ceremony_id"))
    @Column(name = "admission_year")
    @Builder.Default
    private List<String> targetAdmissionYears  = new ArrayList<>();

    @Setter(value = AccessLevel.PRIVATE)
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "ceremony")
    @Builder.Default
    private List<CeremonyAttachImage> ceremonyAttachImageList = new ArrayList<>();

    public void approve() {
        this.ceremonyState = CeremonyState.ACCEPT;
    }

    public void reject() {
        this.ceremonyState = CeremonyState.REJECT;
    }

    public static Ceremony of(
            User user,
            CeremonyCategory ceremonyCategory,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            boolean isSetAll,
            List<String> targetAdmissionYears
    ) {
        return Ceremony.builder()
                .user(user)
                .ceremonyCategory(ceremonyCategory)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .isSetAll(isSetAll)
                .targetAdmissionYears(targetAdmissionYears != null ? targetAdmissionYears : new ArrayList<>())
                .build();
    }

    public static Ceremony createWithImages(
            User user,
            CeremonyCategory ceremonyCategory,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            boolean isSetAll,
            List<String> targetAdmissionYears,
            List<UuidFile> ceremonyAttachImageUuidFileList
    ) {
        Ceremony ceremony = Ceremony.of(
                user,
                ceremonyCategory,
                description,
                startDate,
                endDate,
                isSetAll,
                targetAdmissionYears
        );
        List<CeremonyAttachImage> ceremonyAttachImageList = ceremonyAttachImageUuidFileList.stream()
                .map(uuidFile -> CeremonyAttachImage.of(ceremony, uuidFile))
                .toList();
        ceremony.updateCeremonyAttachImageList(ceremonyAttachImageList);
        return ceremony;
    }

    public void updateCeremonyState(CeremonyState ceremonyState){
        this.ceremonyState = ceremonyState;
    }

    public void updateNote(String note){
        this.note = note;
    }

    public void updateCeremonyAttachImageList(List<CeremonyAttachImage> ceremonyAttachImageList) {
        this.ceremonyAttachImageList = ceremonyAttachImageList;
    }
}
