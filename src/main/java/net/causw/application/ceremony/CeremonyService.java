package net.causw.application.ceremony;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.push.Ceremony;
import net.causw.adapter.persistence.repository.push.CeremonyRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.ceremony.CeremonyResponseDTO;
import net.causw.application.dto.ceremony.CreateCeremonyRequestDTO;
import net.causw.application.dto.ceremony.UpdateCeremonyStateRequestDto;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.ceremony.CeremonyState;
import net.causw.domain.model.enums.uuidFile.FilePath;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CeremonyService {
    private final CeremonyRepository ceremonyRepository;
    private final UuidFileService uuidFileService;
    @Transactional
    public CeremonyResponseDTO createCeremony(
            User user,
            @Valid CreateCeremonyRequestDTO createCeremonyRequestDTO,
            List<MultipartFile> imageFileList
    ) {
        List<UuidFile> uuidFileList = uuidFileService.saveFileList(imageFileList, FilePath.USER_ACADEMIC_RECORD_APPLICATION);
        Ceremony ceremony = Ceremony.createWithImages(
                user,
                createCeremonyRequestDTO.getCategory(),
                createCeremonyRequestDTO.getDescription(),
                createCeremonyRequestDTO.getStartDate(),
                createCeremonyRequestDTO.getEndDate(),
                uuidFileList
        );
        ceremonyRepository.save(ceremony);

        return CeremonyResponseDTO.from(ceremony);
    }

    @Transactional(readOnly = true)
    public List<CeremonyResponseDTO> getUserCeremonyResponsesDTO(User user) {
        List<Ceremony> ceremonies = ceremonyRepository.findAllByUser(user);
        return ceremonies.stream()
                .map(CeremonyResponseDTO::from) // Assuming from method exists in DTO
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CeremonyResponseDTO> getAllUserAwaitingCeremonyPage(Pageable pageable) {
        Page<Ceremony> ceremoniesPage = ceremonyRepository.findByCeremonyState(CeremonyState.AWAIT, pageable);
        return ceremoniesPage.map(CeremonyResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public CeremonyResponseDTO getCeremony(String ceremonyId){
        Ceremony ceremony = ceremonyRepository.findById(ceremonyId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CEREMONY_NOT_FOUND
                )
        );
        return CeremonyResponseDTO.from(ceremony);
    }

    @Transactional
    public CeremonyResponseDTO updateUserCeremonyStatus(User user, UpdateCeremonyStateRequestDto updateDto) {
        // 대상 경조사 조회
        Ceremony ceremony = ceremonyRepository.findById(updateDto.getCeremonyId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CEREMONY_NOT_FOUND
                )
        );
        ceremony.updateCeremonyState(updateDto.getTargetCeremonyState());

        if (updateDto.getTargetCeremonyState() == CeremonyState.REJECT) {
            ceremony.updateNote(updateDto.getRejectMessage());
        }
        ceremonyRepository.save(ceremony);

        return CeremonyResponseDTO.from(ceremony);
    }

    @Transactional
    public void approveCeremonyRequest(Long requestId) {

        // Logic to notify users
    }

    @Transactional
    public void denyCeremonyRequest(Long requestId) {

    }
}
