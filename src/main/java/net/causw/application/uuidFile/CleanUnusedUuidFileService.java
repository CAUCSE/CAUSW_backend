package net.causw.application.uuidFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.adapter.persistence.repository.uuidFile.*;
import net.causw.adapter.persistence.uuidFile.*;
import net.causw.adapter.persistence.uuidFile.joinEntity.*;
import net.causw.domain.model.util.RedisUtils;
import net.causw.domain.model.util.StaticValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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


    public void initIsUsedUuidFileIntegration() {
        Boolean isLast = false;
        do {
            isLast = initIsUsedUuidFile();
        } while (!isLast);
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
        return !uuidFilePage.isLast();
    }

    public void checkIsUsedWithCalendarAttachImageIntegration() {
        Boolean isLast = false;
        do {
            isLast = checkIsUsedWithCalendarAttachImage();
        } while (!isLast);
    }

    @Transactional
    public Boolean checkIsUsedWithCalendarAttachImage() {
        Integer pageNum = getPriorPageNum("calendarAttachImage");
        Page<CalendarAttachImage> calendarAttachImagePage = calendarAttachImageRepository.findAll(
                PageRequest.of(
                        pageNum,
                        StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
        );
        Set<UuidFile> uuidFileSet = calendarAttachImagePage.stream().map(CalendarAttachImage::getUuidFile).collect(Collectors.toSet());
        uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
        uuidFileRepository.saveAll(uuidFileSet);
        pageNum++;
        redisUtils.setPageNumData("calendarAttachImage", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
        return !calendarAttachImagePage.isLast();
    }

    public void checkIsUsedWithCircleMainImageIntegration() {
        Boolean isLast = false;
        do {
            isLast = checkIsUsedWithCircleMainImage();
        } while (!isLast);
    }

    @Transactional
    public Boolean checkIsUsedWithCircleMainImage() {
        Integer pageNum = getPriorPageNum("circleMainImage");
        Page<CircleMainImage> circleMainImagePage = circleMainImageRepository.findAll(
                PageRequest.of(
                        pageNum,
                        StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
        );
        Set<UuidFile> uuidFileSet = circleMainImagePage.stream().map(CircleMainImage::getUuidFile).collect(Collectors.toSet());
        uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
        uuidFileRepository.saveAll(uuidFileSet);
        pageNum++;
        redisUtils.setPageNumData("circleMainImage", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
        return !circleMainImagePage.isLast();
    }

    public void checkIsUsedWithEventAttachImageIntegration() {
        Boolean isLast = false;
        do {
            isLast = checkIsUsedWithEventAttachImage();
        } while (!isLast);
    }

    @Transactional
    public Boolean checkIsUsedWithEventAttachImage() {
        Integer pageNum = getPriorPageNum("eventAttachImage");
        Page<EventAttachImage> eventAttachImagePage = eventAttachImageRepository.findAll(
                PageRequest.of(
                        pageNum,
                        StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
        );
        Set<UuidFile> uuidFileSet = eventAttachImagePage.stream().map(EventAttachImage::getUuidFile).collect(Collectors.toSet());
        uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
        uuidFileRepository.saveAll(uuidFileSet);
        pageNum++;
        redisUtils.setPageNumData("eventAttachImage", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
        return !eventAttachImagePage.isLast();
    }

    public void checkIsUsedWithPostAttachImageIntegration() {
        Boolean isLast = false;
        do {
            isLast = checkIsUsedWithPostAttachImage();
        } while (!isLast);
    }

    @Transactional
    public Boolean checkIsUsedWithPostAttachImage() {
        Integer pageNum = getPriorPageNum("postAttachImage");
        Page<PostAttachImage> postAttachImagePage = postAttachImageRepository.findAll(
                PageRequest.of(
                        pageNum,
                        StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
        );
        Set<UuidFile> uuidFileSet = postAttachImagePage.stream().map(PostAttachImage::getUuidFile).collect(Collectors.toSet());
        uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
        uuidFileRepository.saveAll(uuidFileSet);
        pageNum++;
        redisUtils.setPageNumData("postAttachImage", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
        return !postAttachImagePage.isLast();
    }

    public void checkIsUsedWithUserAcademicRecordApplicationAttachImageIntegration() {
        Boolean isLast = false;
        do {
            isLast = checkIsUsedWithUserAcademicRecordApplicationAttachImage();
        } while (!isLast);
    }

    @Transactional
    public Boolean checkIsUsedWithUserAcademicRecordApplicationAttachImage() {
        Integer pageNum = getPriorPageNum("userAcademicRecordApplicationAttachImage");
        Page<UserAcademicRecordApplicationAttachImage> userAcademicRecordApplicationAttachImagePage = userAcademicRecordApplicationAttachImageRepository.findAll(
                PageRequest.of(
                        pageNum,
                        StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
        );
        Set<UuidFile> uuidFileSet = userAcademicRecordApplicationAttachImagePage.stream().map(UserAcademicRecordApplicationAttachImage::getUuidFile).collect(Collectors.toSet());
        uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
        uuidFileRepository.saveAll(uuidFileSet);
        pageNum++;
        redisUtils.setPageNumData("userAcademicRecordApplicationAttachImage", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
        return !userAcademicRecordApplicationAttachImagePage.isLast();
    }

    public void checkIsUsedWithUserAdmissionAttachImageIntegration() {
        Boolean isLast = false;
        do {
            isLast = checkIsUsedWithUserAdmissionAttachImage();
        } while (!isLast);
    }

    @Transactional
    public Boolean checkIsUsedWithUserAdmissionAttachImage() {
        Integer pageNum = getPriorPageNum("userAdmissionAttachImage");
        Page<UserAdmissionAttachImage> userAdmissionAttachImagePage = userAdmissionAttachImageRepository.findAll(
                PageRequest.of(
                        pageNum,
                        StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
        );
        Set<UuidFile> uuidFileSet = userAdmissionAttachImagePage.stream().map(UserAdmissionAttachImage::getUuidFile).collect(Collectors.toSet());
        uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
        uuidFileRepository.saveAll(uuidFileSet);
        pageNum++;
        redisUtils.setPageNumData("userAdmissionAttachImage", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
        return !userAdmissionAttachImagePage.isLast();
    }

    public void checkIsUsedWithUserAdmissionLogAttachImageIntegration() {
        Boolean isLast = false;
        do {
            isLast = checkIsUsedWithUserAdmissionLogAttachImage();
        } while (!isLast);
    }

    @Transactional
    public Boolean checkIsUsedWithUserAdmissionLogAttachImage() {
        Integer pageNum = getPriorPageNum("userAdmissionLog");
        Page<UserAdmissionLogAttachImage> userAdmissionLogAttachImagePage = userAdmissionLogAttachImageRepository.findAll(
                PageRequest.of(
                        pageNum,
                        StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
        );
        Set<UuidFile> uuidFileSet = userAdmissionLogAttachImagePage.stream().map(UserAdmissionLogAttachImage::getUuidFile).collect(Collectors.toSet());
        uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
        uuidFileRepository.saveAll(uuidFileSet);
        pageNum++;
        redisUtils.setPageNumData("userAdmissionLog", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
        return !userAdmissionLogAttachImagePage.isLast();
    }

    public void checkIsUsedWithUserProfileImageIntegration() {
        Boolean isLast = false;
        do {
            isLast = checkIsUsedWithUserProfileImage();
        } while (!isLast);
    }

    @Transactional
    public Boolean checkIsUsedWithUserProfileImage() {
        Integer pageNum = getPriorPageNum("userProfileImage");
        Page<UserProfileImage> userProfileImagePage = userProfileImageRepository.findAll(
                PageRequest.of(
                        pageNum,
                        StaticValue.SELECT_UNUSED_UUID_FILE_PAGE_SIZE)
        );
        Set<UuidFile> uuidFileSet = userProfileImagePage.stream().map(UserProfileImage::getUuidFile).collect(Collectors.toSet());
        uuidFileSet.forEach(uuidFile -> uuidFile.setIsUsed(true));
        uuidFileRepository.saveAll(uuidFileSet);
        pageNum++;
        redisUtils.setPageNumData("userProfileImage", pageNum, StaticValue.CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME);
        return !userProfileImagePage.isLast();
    }

    @Transactional
    public void deleteFileNotUsed() {
        List<UuidFile> uuidFileList = uuidFileRepository.findAllByIsUsed(false);
        log.error("Delete not used file: {}", uuidFileList.size());
        uuidFileRepository.deleteAll(uuidFileList);
    }

    private Integer getPriorPageNum(String tableName) {
        Integer priorPageNum = redisUtils.getPageNumData(tableName);
        if (priorPageNum == null) {
            priorPageNum = 0;
        }
        return priorPageNum;
    }

}
