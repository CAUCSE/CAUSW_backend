package net.causw.app.main.service.userInfo;

import static net.causw.global.constant.StaticValue.DEFAULT_PAGE_SIZE;

import net.causw.app.main.domain.event.InitialAcademicCertificationEvent;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.domain.validation.PhoneNumberFormatValidator;
import net.causw.app.main.domain.validation.ValidatorBucket;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.dto.userInfo.UserCareerDto;
import net.causw.app.main.dto.util.dtoMapper.UserDtoMapper;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userInfo.UserCareer;
import net.causw.app.main.dto.user.UserUpdateRequestDto;
import net.causw.app.main.dto.userInfo.UserInfoUpdateRequestDto;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.repository.userInfo.UserCareerRepository;
import net.causw.app.main.service.pageable.PageableFactory;
import net.causw.app.main.service.user.UserService;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.constant.MessageUtil;
import org.springframework.data.domain.Page;

import java.util.List;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.repository.userInfo.UserInfoRepository;
import net.causw.app.main.domain.model.entity.userInfo.UserInfo;
import net.causw.app.main.dto.userInfo.UserInfoResponseDto;
import net.causw.app.main.dto.userInfo.UserInfoSummaryResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserInfoService {
    private final UserInfoRepository userInfoRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PageableFactory pageableFactory;
    private final UserCareerRepository userCareerRepository;

    @Transactional(readOnly = true)
    public Page<UserInfoSummaryResponseDto> getAllUserInfos(Integer pageNum) {
        return userInfoRepository.findAllByUserStateOrderByUpdatedAtDesc(UserState.ACTIVE, pageableFactory.create(pageNum, DEFAULT_PAGE_SIZE))
                .map(UserDtoMapper.INSTANCE::toUserInfoSummaryResponseDto);
    }

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfoByUserId(String userId) {
        User user = findUserById(userId);

        if (user.getState() != UserState.ACTIVE) {
            throw new BadRequestException(
                    ErrorCode.INVALID_REQUEST_USER_STATE,
                    MessageUtil.USER_INFO_NOT_ACCESSIBLE
            );
        }

        UserInfo userInfo = userInfoRepository.findByUserId(userId)
                .orElse(UserInfo.of(user));

        return UserDtoMapper.INSTANCE.toUserInfoResponseDto(userInfo);
    }

    @EventListener // 동문수첩 기본 프로필 생성 실패시, 학적 인증과 함께 롤백
    public void createDefaultProfile(InitialAcademicCertificationEvent event) {
        User user = findUserById(event.userId());

        userInfoRepository.findByUserId(event.userId())
                .orElseGet(() -> userInfoRepository.save(UserInfo.of(user)));
    }

    @Transactional
    public UserInfoResponseDto update(String userId, UserInfoUpdateRequestDto request, MultipartFile profileImage) {
        User user = findUserById(userId);

        // 사용자 정보 갱신(전화번호, 프로필 이미지)
        final UserUpdateRequestDto userUpdateRequestDto = UserUpdateRequestDto.builder()
                .nickname(user.getNickname())
                .phoneNumber(request.getPhoneNumber() == null ? user.getPhoneNumber() : request.getPhoneNumber())
                .build();

        userService.update(user, userUpdateRequestDto, profileImage); // 실패시 user, userInfo 전부 rollback

        // 사용자 상세정보 갱신
        final UserInfo userInfo = userInfoRepository.findByUserId(userId)
                .orElseGet(() -> userInfoRepository.save(UserInfo.of(user))); // 없는 경우 생성

        userInfo.update(
                request.getDescription(), request.getJob(),
                request.getGithubLink(), request.getLinkedInLink(), request.getInstagramLink(),
                request.getNotionLink(), request.getBlogLink(),
                request.getIsPhoneNumberVisible());

        // 사용자 커리어 갱신
        updateUserCareer(request.getUserCareer(), userInfo);

        return UserDtoMapper.INSTANCE.toUserInfoResponseDto(userInfo);
    }

    @Transactional
    protected void updateUserCareer(List<UserCareerDto> userCareerDtoList, UserInfo userInfo) {
        Set<String> requestedIdSet = new HashSet<>();

        for (UserCareerDto userCareerDto : userCareerDtoList) {

            validateUserCareerDate(userCareerDto); // 커리어 시작 시점과 종료 시점 검사

            if (userCareerDto.getId() == null) { // 커리어 생성
                UserCareer userCareer = UserCareer.of(
                        userInfo,
                        userCareerDto.getStartYear(), userCareerDto.getStartMonth(),
                        userCareerDto.getEndYear(), userCareerDto.getEndMonth(),
                        userCareerDto.getDescription());

                UserCareer createdCareer = userCareerRepository.save(userCareer);

                requestedIdSet.add(createdCareer.getId()); // 삭제 대상에서 제외

            } else { // 커리어 수정
                UserCareer userCareer = userCareerRepository.findById(userCareerDto.getId())
                        .orElseThrow(() -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.USER_CAREER_NOT_FOUND
                        ));

                userCareer.update(
                        userCareerDto.getStartYear(), userCareerDto.getStartMonth(),
                        userCareerDto.getEndYear(), userCareerDto.getEndMonth(),
                        userCareerDto.getDescription());

                requestedIdSet.add(userCareerDto.getId()); // 삭제 대상에서 제외
            }
        }

        // 커리어 삭제
        List<String> idToDeleteList = userCareerRepository.findAllCareerByUserInfoId(userInfo.getId()).stream()
                .map(BaseEntity::getId)
                .filter(id -> !requestedIdSet.contains(id)).toList();

        if (!idToDeleteList.isEmpty()) {
            userCareerRepository.deleteAllByIdInBatch(idToDeleteList);
        }
    }

    @Transactional(readOnly = true)
    public Page<UserInfoSummaryResponseDto> search(final String keyword, final Integer pageNum) {
        return userInfoRepository.findAllByUserStateAndKeywordInNameOrJobOrCareer(
                        UserState.ACTIVE, keyword, pageableFactory.create(pageNum, DEFAULT_PAGE_SIZE))
                .map(UserDtoMapper.INSTANCE::toUserInfoSummaryResponseDto);
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));
    }

    private void validateUserCareerDate(UserCareerDto userCareerDto) {
        int currentYear = LocalDateTime.now().getYear();

        int startMonth = userCareerDto.getStartMonth();
        int startYear = userCareerDto.getStartYear();
        Integer endMonth = userCareerDto.getEndMonth();
        Integer endYear = userCareerDto.getEndYear();

        // 시작일 검증
        boolean isInvalidStartMonth = startMonth < 1 || startMonth > 12;
        boolean isInvalidStartYear = startYear < 1 || startYear > currentYear;

        if (isInvalidStartMonth || isInvalidStartYear) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    MessageUtil.INVALID_CAREER_DATE
            );
        }

        // 종료일이 있는 경우에만 검증 (null, 0(default)은 현재 재직 중으로 판단)
        if (endYear != null && endYear > 0 && endMonth != null && endMonth > 0) {
            boolean isInvalidEndMonth = endMonth < 1 || endMonth > 12;
            boolean isInvalidEndYear = endYear < 1 || endYear > currentYear;
            boolean isEndBeforeStart = startYear > endYear || (startYear == endYear && startMonth > endMonth);

            if (isInvalidEndMonth || isInvalidEndYear || isEndBeforeStart) {
                throw new BadRequestException(
                        ErrorCode.INVALID_PARAMETER,
                        MessageUtil.INVALID_CAREER_DATE
                );
            }
        }
    }
}
