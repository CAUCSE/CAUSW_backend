package net.causw.app.main.service.semester;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.semester.Semester;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userCouncilFee.CouncilFeeFakeUser;
import net.causw.app.main.domain.model.entity.userCouncilFee.UserCouncilFee;
import net.causw.app.main.domain.model.enums.semester.SemesterType;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.dto.semester.CreateSemesterRequestDto;
import net.causw.app.main.dto.semester.CurrentSemesterResponseDto;
import net.causw.app.main.dto.util.dtoMapper.SemesterDtoMapper;
import net.causw.app.main.infrastructure.aop.annotation.MeasureTime;
import net.causw.app.main.repository.semester.SemesterRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.repository.userCouncilFee.CouncilFeeFakeUserRepository;
import net.causw.app.main.repository.userCouncilFee.UserCouncilFeeRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@MeasureTime
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SemesterService {

	private final SemesterRepository semesterRepository;
	private final UserRepository userRepository;
	private final UserCouncilFeeRepository userCouncilFeeRepository;
	private final CouncilFeeFakeUserRepository councilFeeFakeUserRepository;

	public CurrentSemesterResponseDto getCurrentSemester() {
		List<Semester> currentSemesterList = semesterRepository.findAllByIsCurrent(true);
		if (currentSemesterList.isEmpty()) {
			throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.CURRENT_SEMESTER_DOES_NOT_EXIST);
		}
		return toCurrentSemesterResponseDto(currentSemesterList.get(0));
	}

	public Semester getCurrentSemesterEntity() {
		List<Semester> currentSemesterList = semesterRepository.findAllByIsCurrent(true);
		if (currentSemesterList.isEmpty()) {
			throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.CURRENT_SEMESTER_DOES_NOT_EXIST);
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
	public void createSemester(CreateSemesterRequestDto createSemesterRequestDto, User user) {
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
	}

	@Transactional
	public void createNextSemester(User user) {
		// 신학기 시작
		Semester priorSemester = getCurrentSemesterEntity();

		priorSemester.updateIsCurrent(false);

		Semester newSemester = (priorSemester.getSemesterType().equals(SemesterType.FIRST)) ?
			Semester.of(priorSemester.getSemesterYear(), SemesterType.SECOND, user) :
			Semester.of(priorSemester.getSemesterYear() + 1, SemesterType.FIRST, user);

		semesterRepository.save(priorSemester);
		semesterRepository.save(newSemester);

		// 신학기 시작으로 학적상태가 재학 또는 휴학인 학생들을 미결정으로 변경
		List<User> userList = userRepository.findByAcademicStatusInOrAcademicStatusIsNull(
				List.of(
					AcademicStatus.ENROLLED,
					AcademicStatus.LEAVE_OF_ABSENCE
				))
			.stream()
			.peek(
				(u) -> u.setAcademicStatus(AcademicStatus.UNDETERMINED)
			)
			.toList();

		userRepository.saveAll(userList);

		// 동문네트워크 서비스 미가입 FakeUser 중, 현재 등록 완료 학기 +1
		List<CouncilFeeFakeUser> councilFeeFakeUserList = userCouncilFeeRepository.findAllByIsJoinedService(false)
			.stream()
			.map(UserCouncilFee::getCouncilFeeFakeUser)
			.filter(councilFeeFakeUser -> councilFeeFakeUser.getAcademicStatus().equals(AcademicStatus.ENROLLED))
			.peek(councilFeeFakeUser -> {
				validAcademicStatusAndCurrentCompletedSemester(councilFeeFakeUser.getAcademicStatus(),
					councilFeeFakeUser.getCurrentCompletedSemester() + 1
				);
				councilFeeFakeUser.setCurrentCompletedSemester(
					councilFeeFakeUser.getCurrentCompletedSemester() + 1);
			})
			.toList();

		councilFeeFakeUserRepository.saveAll(councilFeeFakeUserList);
	}

	@Transactional
	public void deleteSemester(String semesterId) {
		semesterRepository.deleteById(semesterId);
	}

	private CurrentSemesterResponseDto toCurrentSemesterResponseDto(Semester semester) {
		return SemesterDtoMapper.INSTANCE.toCurrentSemesterResponseDto(semester);
	}

	// Private Methods
	public void validAcademicStatusAndCurrentCompletedSemester(AcademicStatus academicStatus,
		Integer currentCompletedSemester) {
		if (academicStatus.equals(AcademicStatus.ENROLLED) && currentCompletedSemester == null) {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_COUNCIL_FEE_FAKE_USER_INFO);
		}
	}
}
