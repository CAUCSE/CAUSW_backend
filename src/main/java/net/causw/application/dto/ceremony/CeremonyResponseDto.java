package net.causw.application.dto.ceremony;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.enums.ceremony.CeremonyCategory;
import net.causw.adapter.persistence.ceremony.Ceremony;
import net.causw.domain.model.enums.ceremony.CeremonyState;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class CeremonyResponseDto {

    @Schema(description = "경조사 id", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "행사 설명", example = "연례 졸업식")
    private String description;

    @Schema(description = "행사 시작 날짜", example = "2025-05-01")
    private LocalDate startDate;

    @Schema(description = "행사 종료 날짜", example = "2025-05-02")
    private LocalDate endDate;

    @Schema(description = "행사 카테고리", example = "GRADUATION")
    private CeremonyCategory category;

    @Schema(description = "신청한 경조사 상태", example = "AWAIT")
    private CeremonyState ceremonyState;

    @Schema(description = "첨부 이미지 URL 리스트")
    private List<String> attachedImageUrlList;

    public static CeremonyResponseDto from(Ceremony ceremony) {
        List<String> attachedImageUrlList = ceremony.getCeremonyAttachImageList().stream()
                .map(image -> image.getUuidFile().getFileUrl()) // Get URL from UuidFile
                .collect(Collectors.toList());

        return CeremonyResponseDto.builder()
                .description(ceremony.getDescription())
                .startDate(ceremony.getStartDate())
                .endDate(ceremony.getEndDate())
                .category(ceremony.getCeremonyCategory())
                .ceremonyState(ceremony.getCeremonyState())
                .attachedImageUrlList(attachedImageUrlList)
                .build();
    }
}
