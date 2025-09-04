package net.causw.app.main.service.uuidFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.batch.core.StepExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.CalendarAttachImage;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.CircleMainImage;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.EventAttachImage;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.PostAttachImage;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserAcademicRecordApplicationAttachImage;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserAdmissionAttachImage;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserAdmissionLogAttachImage;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserProfileImage;
import net.causw.app.main.infrastructure.aop.annotation.MeasureTime;
import net.causw.app.main.infrastructure.redis.RedisUtils;
import net.causw.app.main.repository.uuidFile.CalendarAttachImageRepository;
import net.causw.app.main.repository.uuidFile.CircleMainImageRepository;
import net.causw.app.main.repository.uuidFile.EventAttachImageRepository;
import net.causw.app.main.repository.uuidFile.PostAttachImageRepository;
import net.causw.app.main.repository.uuidFile.UserAcademicRecordApplicationAttachImageRepository;
import net.causw.app.main.repository.uuidFile.UserAdmissionAttachImageRepository;
import net.causw.app.main.repository.uuidFile.UserAdmissionLogAttachImageRepository;
import net.causw.app.main.repository.uuidFile.UserProfileImageRepository;
import net.causw.app.main.repository.uuidFile.UuidFileRepository;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@MeasureTime
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CleanUnusedUuidFileService {

	private final RedisUtils redisUtils;
	private final UuidFileRepository uuidFileRepository;
	private final CalendarAttachImageRepository calendarAttachImageRepository;
	private final CircleMainImageRepository circleMainImageRepository;
	private final EventAttachImageRepository eventAttachImageRepository;
	private final PostAttachImageRepository postAttachImageRepository;
	private final UserAcademicRecordApplicationAttachImageRepository userAcademicRecordApplicationAttachImageRepository;
	private final UserAdmissionAttachImageRepository userAdmissionAttachImageRepository;
	private final UserAdmissionLogAttachImageRepository userAdmissionLogAttachImageRepository;
	private final UserProfileImageRepository userProfileImageRepository;

	public void initIsUsedUuidFileIntegration(StepExecution stepExecution) {
		Boolean isLast = false;
		do {
			isLast = initIsUsedUuidFile();
		} while (!isLast);

		stepExecution.getJobExecution().getExecutionContext().putInt(
			"dataRow",
			getPriorPageNum("uuidFile") * StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE
		);

		redisUtils.setPageNumData("uuidFile", -1, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
	}

	@Transactional
	public Boolean initIsUsedUuidFile() {
		Integer pageNum = getPriorPageNum("uuidFile");
		Page<UuidFile> uuidFilePage = uuidFileRepository.findAll(
			PageRequest.of(
				pageNum,
				StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
		);
		uuidFilePage.forEach(uuidFile -> uuidFile.setIsUsed(false));
		uuidFileRepository.saveAll(uuidFilePage);
		pageNum++;
		redisUtils.setPageNumData("uuidFile", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
		return !uuidFilePage.isLast() ||
			uuidFilePage.isEmpty() ||
			uuidFilePage.getTotalElements() == 0 ||
			uuidFilePage.getTotalPages() == 0 ||
			uuidFilePage.getSize() == 0 ||
			!uuidFilePage.hasNext();
	}

	public void checkIsUsedWithCalendarAttachImageIntegration(StepExecution stepExecution) {
		Boolean isLast = false;
		do {
			isLast = checkIsUsedWithCalendarAttachImage();
		} while (!isLast);

		stepExecution.getJobExecution().getExecutionContext().putInt(
			"dataRow",
			getPriorPageNum("calendarAttachImage") * StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE
		);

		redisUtils.setPageNumData("calendarAttachImage", -1, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
	}

	@Transactional
	public Boolean checkIsUsedWithCalendarAttachImage() {
		Integer pageNum = getPriorPageNum("calendarAttachImage");
		Page<CalendarAttachImage> calendarAttachImagePage = calendarAttachImageRepository.findAll(
			PageRequest.of(
				pageNum,
				StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
		);
		Set<UuidFile> uuidFileSet = calendarAttachImagePage.stream()
			.map(CalendarAttachImage::getUuidFile)
			.collect(Collectors.toSet());
		uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
		uuidFileRepository.saveAll(uuidFileSet);
		pageNum++;
		redisUtils.setPageNumData("calendarAttachImage", pageNum,
			StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
		return !calendarAttachImagePage.isLast() ||
			calendarAttachImagePage.isEmpty() ||
			calendarAttachImagePage.getTotalElements() == 0 ||
			calendarAttachImagePage.getTotalPages() == 0 ||
			calendarAttachImagePage.getSize() == 0 ||
			!calendarAttachImagePage.hasNext();
	}

	public void checkIsUsedWithCircleMainImageIntegration(StepExecution stepExecution) {
		Boolean isLast = false;
		do {
			isLast = checkIsUsedWithCircleMainImage();
		} while (!isLast);

		stepExecution.getJobExecution().getExecutionContext().putInt(
			"dataRow",
			getPriorPageNum("circleMainImage") * StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE
		);

		redisUtils.setPageNumData("circleMainImage", -1, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
	}

	@Transactional
	public Boolean checkIsUsedWithCircleMainImage() {
		Integer pageNum = getPriorPageNum("circleMainImage");
		Page<CircleMainImage> circleMainImagePage = circleMainImageRepository.findAll(
			PageRequest.of(
				pageNum,
				StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
		);
		Set<UuidFile> uuidFileSet = circleMainImagePage.stream()
			.map(CircleMainImage::getUuidFile)
			.collect(Collectors.toSet());
		uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
		uuidFileRepository.saveAll(uuidFileSet);
		pageNum++;
		redisUtils.setPageNumData("circleMainImage", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
		return !circleMainImagePage.isLast() ||
			circleMainImagePage.isEmpty() ||
			circleMainImagePage.getTotalElements() == 0 ||
			circleMainImagePage.getTotalPages() == 0 ||
			circleMainImagePage.getSize() == 0 ||
			!circleMainImagePage.hasNext();
	}

	public void checkIsUsedWithEventAttachImageIntegration(StepExecution stepExecution) {
		Boolean isLast = false;
		do {
			isLast = checkIsUsedWithEventAttachImage();
		} while (!isLast);

		stepExecution.getJobExecution().getExecutionContext().putInt(
			"dataRow",
			getPriorPageNum("eventAttachImage") * StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE
		);

		redisUtils.setPageNumData("eventAttachImage", -1, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
	}

	@Transactional
	public Boolean checkIsUsedWithEventAttachImage() {
		Integer pageNum = getPriorPageNum("eventAttachImage");
		Page<EventAttachImage> eventAttachImagePage = eventAttachImageRepository.findAll(
			PageRequest.of(
				pageNum,
				StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
		);
		Set<UuidFile> uuidFileSet = eventAttachImagePage.stream()
			.map(EventAttachImage::getUuidFile)
			.collect(Collectors.toSet());
		uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
		uuidFileRepository.saveAll(uuidFileSet);
		pageNum++;
		redisUtils.setPageNumData("eventAttachImage", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
		return !eventAttachImagePage.isLast() ||
			eventAttachImagePage.isEmpty() ||
			eventAttachImagePage.getTotalElements() == 0 ||
			eventAttachImagePage.getTotalPages() == 0 ||
			eventAttachImagePage.getSize() == 0 ||
			!eventAttachImagePage.hasNext();
	}

	public void checkIsUsedWithPostAttachImageIntegration(StepExecution stepExecution) {
		Boolean isLast = false;
		do {
			isLast = checkIsUsedWithPostAttachImage();
		} while (!isLast);

		stepExecution.getJobExecution().getExecutionContext().putInt(
			"dataRow",
			getPriorPageNum("postAttachImage") * StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE
		);

		redisUtils.setPageNumData("postAttachImage", -1, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
	}

	@Transactional
	public Boolean checkIsUsedWithPostAttachImage() {
		Integer pageNum = getPriorPageNum("postAttachImage");
		Page<PostAttachImage> postAttachImagePage = postAttachImageRepository.findAll(
			PageRequest.of(
				pageNum,
				StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
		);
		Set<UuidFile> uuidFileSet = postAttachImagePage.stream()
			.map(PostAttachImage::getUuidFile)
			.collect(Collectors.toSet());
		uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
		uuidFileRepository.saveAll(uuidFileSet);
		pageNum++;
		redisUtils.setPageNumData("postAttachImage", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
		return !postAttachImagePage.isLast() ||
			postAttachImagePage.isEmpty() ||
			postAttachImagePage.getTotalElements() == 0 ||
			postAttachImagePage.getTotalPages() == 0 ||
			postAttachImagePage.getSize() == 0 ||
			!postAttachImagePage.hasNext();
	}

	public void checkIsUsedWithUserAcademicRecordApplicationAttachImageIntegration(StepExecution stepExecution) {
		Boolean isLast = false;
		do {
			isLast = checkIsUsedWithUserAcademicRecordApplicationAttachImage();
		} while (!isLast);

		stepExecution.getJobExecution().getExecutionContext().putInt(
			"dataRow",
			getPriorPageNum("userAcademicRecordApplicationAttachImage") * StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE
		);

		redisUtils.setPageNumData("userAcademicRecordApplicationAttachImage", -1,
			StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
	}

	@Transactional
	public Boolean checkIsUsedWithUserAcademicRecordApplicationAttachImage() {
		Integer pageNum = getPriorPageNum("userAcademicRecordApplicationAttachImage");
		Page<UserAcademicRecordApplicationAttachImage> userAcademicRecordApplicationAttachImagePage = userAcademicRecordApplicationAttachImageRepository.findAll(
			PageRequest.of(
				pageNum,
				StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
		);
		Set<UuidFile> uuidFileSet = userAcademicRecordApplicationAttachImagePage.stream()
			.map(UserAcademicRecordApplicationAttachImage::getUuidFile)
			.collect(Collectors.toSet());
		uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
		uuidFileRepository.saveAll(uuidFileSet);
		pageNum++;
		redisUtils.setPageNumData("userAcademicRecordApplicationAttachImage", pageNum,
			StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
		return !userAcademicRecordApplicationAttachImagePage.isLast() ||
			userAcademicRecordApplicationAttachImagePage.isEmpty() ||
			userAcademicRecordApplicationAttachImagePage.getTotalElements() == 0 ||
			userAcademicRecordApplicationAttachImagePage.getTotalPages() == 0 ||
			userAcademicRecordApplicationAttachImagePage.getSize() == 0 ||
			!userAcademicRecordApplicationAttachImagePage.hasNext();
	}

	public void checkIsUsedWithUserAdmissionAttachImageIntegration(StepExecution stepExecution) {
		Boolean isLast = false;
		do {
			isLast = checkIsUsedWithUserAdmissionAttachImage();
		} while (!isLast);

		stepExecution.getJobExecution().getExecutionContext().putInt(
			"dataRow",
			getPriorPageNum("userAdmissionAttachImage") * StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE
		);

		redisUtils.setPageNumData("userAdmissionAttachImage", -1,
			StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
	}

	@Transactional
	public Boolean checkIsUsedWithUserAdmissionAttachImage() {
		Integer pageNum = getPriorPageNum("userAdmissionAttachImage");
		Page<UserAdmissionAttachImage> userAdmissionAttachImagePage = userAdmissionAttachImageRepository.findAll(
			PageRequest.of(
				pageNum,
				StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
		);
		Set<UuidFile> uuidFileSet = userAdmissionAttachImagePage.stream()
			.map(UserAdmissionAttachImage::getUuidFile)
			.collect(Collectors.toSet());
		uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
		uuidFileRepository.saveAll(uuidFileSet);
		pageNum++;
		redisUtils.setPageNumData("userAdmissionAttachImage", pageNum,
			StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
		return !userAdmissionAttachImagePage.isLast() ||
			userAdmissionAttachImagePage.isEmpty() ||
			userAdmissionAttachImagePage.getTotalElements() == 0 ||
			userAdmissionAttachImagePage.getTotalPages() == 0 ||
			userAdmissionAttachImagePage.getSize() == 0 ||
			!userAdmissionAttachImagePage.hasNext();
	}

	public void checkIsUsedWithUserAdmissionLogAttachImageIntegration(StepExecution stepExecution) {
		Boolean isLast = false;
		do {
			isLast = checkIsUsedWithUserAdmissionLogAttachImage();
		} while (!isLast);

		stepExecution.getJobExecution().getExecutionContext().putInt(
			"dataRow",
			getPriorPageNum("userAdmissionLog") * StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE
		);

		redisUtils.setPageNumData("userAdmissionLog", -1, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
	}

	@Transactional
	public Boolean checkIsUsedWithUserAdmissionLogAttachImage() {
		Integer pageNum = getPriorPageNum("userAdmissionLog");
		Page<UserAdmissionLogAttachImage> userAdmissionLogAttachImagePage = userAdmissionLogAttachImageRepository.findAll(
			PageRequest.of(
				pageNum,
				StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
		);
		Set<UuidFile> uuidFileSet = userAdmissionLogAttachImagePage.stream()
			.map(UserAdmissionLogAttachImage::getUuidFile)
			.collect(Collectors.toSet());
		uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
		uuidFileRepository.saveAll(uuidFileSet);
		pageNum++;
		redisUtils.setPageNumData("userAdmissionLog", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
		return !userAdmissionLogAttachImagePage.isLast() ||
			userAdmissionLogAttachImagePage.isEmpty() ||
			userAdmissionLogAttachImagePage.getTotalElements() == 0 ||
			userAdmissionLogAttachImagePage.getTotalPages() == 0 ||
			userAdmissionLogAttachImagePage.getSize() == 0 ||
			!userAdmissionLogAttachImagePage.hasNext();
	}

	public void checkIsUsedWithUserProfileImageIntegration(StepExecution stepExecution) {
		Boolean isLast = false;
		do {
			isLast = checkIsUsedWithUserProfileImage();
		} while (!isLast);

		stepExecution.getJobExecution().getExecutionContext().putInt(
			"dataRow",
			getPriorPageNum("userProfileImage") * StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE
		);

		redisUtils.setPageNumData("userProfileImage", -1, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
	}

	@Transactional
	public Boolean checkIsUsedWithUserProfileImage() {
		Integer pageNum = getPriorPageNum("userProfileImage");
		Page<UserProfileImage> userProfileImagePage = userProfileImageRepository.findAll(
			PageRequest.of(
				pageNum,
				StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
		);
		Set<UuidFile> uuidFileSet = userProfileImagePage.stream()
			.map(UserProfileImage::getUuidFile)
			.collect(Collectors.toSet());
		uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
		uuidFileRepository.saveAll(uuidFileSet);
		pageNum++;
		redisUtils.setPageNumData("userProfileImage", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
		return !userProfileImagePage.isLast() ||
			userProfileImagePage.isEmpty() ||
			userProfileImagePage.getTotalElements() == 0 ||
			userProfileImagePage.getTotalPages() == 0 ||
			userProfileImagePage.getSize() == 0 ||
			!userProfileImagePage.hasNext();
	}

	@Transactional
	public void deleteFileNotUsed(StepExecution stepExecution) {
		List<UuidFile> uuidFileList = uuidFileRepository.findAllByIsUsed(false);

		int deletedFileCount = uuidFileList.size();
		log.info("Delete not used file: {}", uuidFileList.size());
		uuidFileRepository.deleteAll(uuidFileList);
		stepExecution.getJobExecution().getExecutionContext().putInt("deletedFileCount", deletedFileCount);
	}

	private Integer getPriorPageNum(String tableName) {
		Integer priorPageNum = redisUtils.getPageNumData(tableName);
		if (priorPageNum == null || priorPageNum < 0) {
			priorPageNum = 0;
		}
		return priorPageNum;
	}

}
