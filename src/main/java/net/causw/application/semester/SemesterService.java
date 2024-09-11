package net.causw.application.semester;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.repository.SemesterRepository;
import net.causw.adapter.persistence.semester.Semester;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.semester.CreateSemesterRequestDto;
import net.causw.application.dto.semester.CurrentSemesterResponseDto;
import net.causw.application.dto.util.SemesterDtoMapper;
import net.causw.domain.model.enums.SemesterType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SemesterService {

    private final SemesterRepository semesterRepository;

    public CurrentSemesterResponseDto getCurrentSemester() {
        List<Semester> currentSemesterList = semesterRepository.findAllByIsCurrent(true);
        if (currentSemesterList.isEmpty()) {
            return null;
        }
        return toCurrentSemesterResponseDto(currentSemesterList.get(0));
    }

    public Semester getCurrentSemesterEntity() {
        List<Semester> currentSemesterList = semesterRepository.findAllByIsCurrent(true);
        if (currentSemesterList.isEmpty()) {
            return null;
        }
        return currentSemesterList.get(0);
    }

    public List<CurrentSemesterResponseDto> getSemesterList() {
        List<Semester> semesterList = semesterRepository.findAll();
        return semesterList.stream()
                .map(this::toCurrentSemesterResponseDto)
                .toList();
    }

    @Transactional
    public Void createSemester(CreateSemesterRequestDto createSemesterRequestDto, User user) {
        List<Semester> currentSemesterList = semesterRepository.findAllByIsCurrent(true);

        if (!currentSemesterList.isEmpty()) {
            Semester currentSemester = currentSemesterList.get(0);
            currentSemester.updateIsCurrent(false);
            semesterRepository.save(currentSemester);
        }

        Semester semester = Semester.of(
                createSemesterRequestDto.getSemesterYear(),
                createSemesterRequestDto.getSemesterType(),
                user
        );
        semesterRepository.save(semester);
        return null;
    }

    @Transactional
    public Void deleteSemester(String semesterId) {
        semesterRepository.deleteById(semesterId);
        return null;
    }

    private CurrentSemesterResponseDto toCurrentSemesterResponseDto(Semester semester) {
        return SemesterDtoMapper.INSTANCE.toCurrentSemesterResponseDto(semester);
    }
}
