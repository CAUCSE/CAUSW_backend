package net.causw.application.circle;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.form.*;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.board.BoardRepository;
import net.causw.adapter.persistence.repository.circle.CircleMemberRepository;
import net.causw.adapter.persistence.repository.circle.CircleRepository;
import net.causw.adapter.persistence.repository.form.FormRepository;
import net.causw.adapter.persistence.repository.form.QuestionRepository;
import net.causw.adapter.persistence.repository.form.ReplyRepository;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.repository.userCouncilFee.UserCouncilFeeRepository;
import net.causw.adapter.persistence.repository.uuidFile.CircleMainImageRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userCouncilFee.UserCouncilFee;
import net.causw.adapter.persistence.uuidFile.joinEntity.CircleMainImage;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.board.BoardOfCircleResponseDto;
import net.causw.application.dto.circle.*;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.form.request.FormReplyRequestDto;
import net.causw.application.dto.form.request.QuestionReplyRequestDto;
import net.causw.application.dto.form.request.create.FormCreateRequestDto;
import net.causw.application.dto.form.response.FormResponseDto;
import net.causw.application.dto.form.response.OptionResponseDto;
import net.causw.application.dto.form.response.QuestionResponseDto;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.util.dtoMapper.CircleDtoMapper;
import net.causw.application.dto.util.StatusUtil;
import net.causw.application.dto.util.dtoMapper.FormDtoMapper;
import net.causw.application.excel.CircleExcelService;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.circle.CircleMemberStatus;
import net.causw.domain.model.enums.form.QuestionType;
import net.causw.domain.model.enums.form.RegisteredSemester;
import net.causw.domain.model.enums.form.RegisteredSemesterManager;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.enums.uuidFile.FilePath;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.causw.application.dto.board.BoardOfCircleResponseDto.isWriteable;
@MeasureTime
@Service
@RequiredArgsConstructor
public class CircleService {
    private final CircleExcelService circleExcelService;
    private final Validator validator;
    private final CircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final UuidFileService uuidFileService;
    private final CircleMainImageRepository circleMainImageRepository;
    private final UserCouncilFeeRepository userCouncilFeeRepository;
    private final FormRepository formRepository;
    private final ReplyRepository replyRepository;
    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public CircleResponseDto findById(String circleId) {
        Circle circle = getCircle(circleId);

        initializeValidator(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE).validate();

        return this.toCircleResponseDtoExtended(circle, getCircleNumMember(circleId));
    }

    @Transactional(readOnly = true)
    public List<CirclesResponseDto> findAll(User user) {
        Set<Role> roles = user.getRoles();

        initializeUserValidator(user.getState(), roles).validate();

        Map<String, CircleMember> joinedCircleMap = circleMemberRepository.findByUser_Id(user.getId())
                .stream()
                .filter(circleMember -> circleMember.getStatus().equals(CircleMemberStatus.MEMBER))
                .collect(Collectors.toMap(
                        circleMember -> circleMember.getCircle().getId(),
                        circleMember -> circleMember
                ));

        return circleRepository.findAll()
                .stream()
                .map(circle -> {
                    if (StatusUtil.isAdminOrPresident(user)) {
                        return this.toCirclesResponseDtoExtended(
                                circle,
                                getCircleNumMember(circle.getId()),
                                LocalDateTime.now()
                        );
                    } else {
                        if (joinedCircleMap.containsKey(circle.getId())) {
                            return this.toCirclesResponseDtoExtended(
                                    circle,
                                    getCircleNumMember(circle.getId()),
                                    joinedCircleMap.get(circle.getId()).getUpdatedAt()
                            );
                        } else {
                            return this.toCirclesResponseDto(
                                    circle,
                                    getCircleNumMember(circle.getId())
                            );
                        }
                    }
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CircleBoardsResponseDto findBoards(
            User user,
            String circleId
    ) {
        Set<Role> roles = user.getRoles();
        Circle circle = getCircle(circleId);

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .validate();

        if (!StatusUtil.isAdminOrPresident(user)) {
            CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(user.getId(), circleId).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.ROW_DOES_NOT_EXIST,
                            MessageUtil.CIRCLE_APPLY_INVALID
                    )
            );

            ValidatorBucket.of()
                    .consistOf(CircleMemberStatusValidator.of(
                            circleMember.getStatus(),
                            List.of(CircleMemberStatus.MEMBER)
                    ))
                    .validate();
        }

        return this.toCircleBoardsResponseDto(
                circle,
                getCircleNumMember(circleId),
                boardRepository.findByCircle_IdAndIsDeletedIsFalseOrderByCreatedAtAsc(circleId)
                        .stream()
                        .map(board -> postRepository.findTop1ByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(board.getId())
                                    .map(post -> this.toBoardOfCircleResponseDtoExtended(
                                            board,
                                            roles,
                                            post,
                                            postRepository.countAllCommentByPost_Id(post.getId())
                                    )).orElse(
                                        this.toBoardOfCircleResponseDto(
                                                    board,
                                                    roles
                                            ))
                        ).collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public Long getNumMember(String id) {
        return getCircleNumMember(getCircle(id).getId());
    }

    @Transactional(readOnly = true)
    public List<CircleMemberResponseDto> getUserList(
            User user,
            String circleId,
            CircleMemberStatus status
    ) {
        Set<Role> roles = user.getRoles();

        Circle circle = getCircle(circleId);

        User circleLeader = getCircleLeader(circle);

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(roles,
                        Set.of(Role.LEADER_CIRCLE)))
                .consistOf(UserEqualValidator.of(user.getId(), circleLeader.getId()))
                .validate();

        return circleMemberRepository.findByCircle_Id(circle.getId())
                .stream()
                .filter(circleMember -> circleMember.getStatus().equals(status))
                .map(circleMember -> this.toCircleMemberResponseDto(
                        circleMember,
                        circleMember.getCircle(),
                        userRepository.findById(circleMember.getUser().getId()).orElseThrow(
                                () -> new BadRequestException(
                                        ErrorCode.ROW_DOES_NOT_EXIST,
                                        MessageUtil.USER_NOT_FOUND
                                )
                        )
                )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CircleMemberResponseDto> getMemberList(String circleId) {
        Circle circle = getCircle(circleId);

        return circleMemberRepository.findByCircle_Id(circle.getId())
                .stream()
                .filter(circleMember -> circleMember.getStatus().equals(CircleMemberStatus.MEMBER))
                .map(circleMember -> this.toCircleMemberResponseDto(
                        circleMember,
                        circleMember.getCircle(),
                        userRepository.findById(circleMember.getUser().getId()).orElseThrow(
                                () -> new BadRequestException(
                                        ErrorCode.ROW_DOES_NOT_EXIST,
                                        MessageUtil.USER_NOT_FOUND
                                )
                        )
                )).collect(Collectors.toList());
    }


    @Transactional
    public void create(CircleCreateRequestDto circleCreateRequestDto, MultipartFile mainImage) {
        User leader = userRepository.findById(circleCreateRequestDto.getLeaderId())
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.NEW_CIRCLE_LEADER_NOT_FOUND)
                );

        UuidFile uuidFile = (mainImage == null) ?
                null :
                uuidFileService.saveFile(mainImage, FilePath.CIRCLE_PROFILE);

        Circle circle = Circle.of(
                circleCreateRequestDto.getName(),
                uuidFile,
                circleCreateRequestDto.getDescription(),
                false,
                circleCreateRequestDto.getCircleTax(),
                circleCreateRequestDto.getRecruitMembers(),
                leader,
                circleCreateRequestDto.getRecruitEndDate(),
                circleCreateRequestDto.getIsRecruit()
        );

        /* Check if the request user is president or admin
         * Then, validate the circle name whether it is duplicated or not
         */
        circleRepository.findByName(circle.getName()).ifPresent(
                name -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.CIRCLE_DUPLICATE_NAME
                    );
                }
        );

        // Grant role to the LEADER
        leader = updateRole(leader, Role.LEADER_CIRCLE);

        // Create circle
        circleRepository.save(circle);

        // Create boards of circle
        Board noticeBoard = Board.of(
                circle.getName() + "공지 게시판",
                circle.getName() + "공지 게시판",
                Stream.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT, Role.LEADER_CIRCLE)
                        .map(Role::getValue)
                        .collect(Collectors.toList()),
                "동아리 공지 게시판",
                false,
                circle
        );
        boardRepository.save(noticeBoard);


        // Apply the leader automatically to the circle
        CircleMember circleMember = circleMemberRepository.save(CircleMember.of(
                circle,
                leader,
                null,
                null
        ));

        updateCircleMemberStatus(circleMember.getId(), CircleMemberStatus.MEMBER);
    }

    @Transactional
    public CircleResponseDto update(
            User user,
            String circleId,
            CircleUpdateRequestDto circleUpdateRequestDto,
            MultipartFile mainImage
    ) {
        Circle circle = getCircle(circleId);
        Set<Role> roles = user.getRoles();

        if (!circle.getName().equals(circleUpdateRequestDto.getName())) {
            circleRepository.findByName(circleUpdateRequestDto.getName()).ifPresent(
                    name -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_ALREADY_EXIST,
                                MessageUtil.CIRCLE_DUPLICATE_NAME
                        );
                    }
            );
        }

        ValidatorBucket validatorBucket = ValidatorBucket.of();

        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(ConstraintValidator.of(circle, this.validator))
                .consistOf(UserRoleValidator.of(
                        roles,
                        Set.of(Role.LEADER_CIRCLE)
                ));

        if (roles.contains(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            getCircleLeader(circle).getId(),
                            user.getId()
                    ));
        }

        validatorBucket
                .validate();


        // 이미지가 없을 경우 기존 이미지를 삭제, 이미지가 있을 경우 새로운 이미지로 교체 (Circle의 이미지는 not null임)
        CircleMainImage circleMainImage = null;

        if (mainImage.isEmpty()) {
            if (circle.getCircleMainImage() != null) {
                uuidFileService.deleteFile(circle.getCircleMainImage().getUuidFile());
                circleMainImageRepository.delete(circle.getCircleMainImage());
            }
        } else {
            if (circle.getCircleMainImage() == null) {
                circleMainImage = CircleMainImage.of(
                        circle,
                        uuidFileService.saveFile(mainImage, FilePath.CIRCLE_PROFILE)
                );
            } else {
                circleMainImage = circle.getCircleMainImage().updateUuidFileAndReturnSelf(
                        uuidFileService.updateFile(
                                circle.getCircleMainImage().getUuidFile(),
                                mainImage,
                                FilePath.CIRCLE_PROFILE
                        )
                );
            }
        }

        circle.update(
                circleUpdateRequestDto.getName(),
                circleUpdateRequestDto.getDescription(),
                circleMainImage,
                circleUpdateRequestDto.getCircleTax(),
                circleUpdateRequestDto.getRecruitMembers(),
                circleUpdateRequestDto.getRecruitEndDate(),
                circleUpdateRequestDto.getIsRecruit()
        );

        return this.toCircleResponseDto(circleRepository.save(circle));
    }

    @Transactional
    public CircleResponseDto delete(
            User user,
            String circleId
    ) {
        Circle circle = getCircle(circleId);

        Set<Role> roles = user.getRoles();

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(
                        roles,
                        Set.of(Role.LEADER_CIRCLE)
                ));

        if (roles.contains(Role.LEADER_CIRCLE)) {
            User leader = circle.getLeader().orElse(null);
            if (leader == null) {
                throw new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.CIRCLE_WITHOUT_LEADER
                );
            }
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            leader.getId(),
                            user.getId()
                    ));
        }

        validatorBucket
                .validate();

        // Change leader role to COMMON
        User leader = getCircleLeader(circle);

        List<Circle> ownCircleList = circleRepository.findByLeader_Id(leader.getId());

        if (ownCircleList.size() == 1) {
            this.removeRole(leader, Role.LEADER_CIRCLE);
        }

        CircleResponseDto circleResponseDto = this.toCircleResponseDto(deleteCircle(circleId).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));

        deleteAllCircleBoard(circleId);

        List<Form> formList = formRepository.findAllByCircle(circle);

        formList.forEach(form -> {
            form.setIsDeleted(true);
            form.setIsClosed(true);
        });

        formRepository.saveAll(formList);

        return circleResponseDto;
    }

    @Transactional
    public void userApply(User user, String circleId, FormReplyRequestDto formReplyRequestDto) {
        Circle circle = getCircle(circleId);
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(StudentIdIsNullValidator.of(user.getStudentId()))
                .validate();

        CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(user.getId(), circle.getId())
                .orElseGet(() -> CircleMember.of(
                        circle,
                        user,
                        null,
                        null
                ));

        Reply reply = this.replyForm(
                this.getForm(circle),
                formReplyRequestDto,
                user
        );

        circleMember.setAppliedForm(reply.getForm());
        circleMember.setAppliedReply(reply);

        replyRepository.save(reply);

        circleMemberRepository.save(circleMember);
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckResponseDto isDuplicatedName(String name) {
        return this.toDuplicatedCheckResponseDto(circleRepository.findByName(name).isPresent());
    }

    @Transactional
    public CircleMemberResponseDto leaveUser(User user, String circleId) {
        Set<Role> roles = user.getRoles();

        Circle circle = getCircle(circleId);

        CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(user.getId(), circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.MEMBER)
                ))
                .consistOf(UserNotEqualValidator.of(
                        getCircleLeader(circle).getId(),
                        user.getId())
                )
                .validate();

        return this.toCircleMemberResponseDto(
                updateCircleMemberStatus(circleMember.getId(), CircleMemberStatus.LEAVE),
                circle,
                user
        );
    }

    @Transactional
    public CircleMemberResponseDto dropUser(
            User requestUser,
            String userId,
            String circleId
    ) {
        Set<Role> roles = requestUser.getRoles();

        User user = getUser(userId);

        Circle circle = getCircle(circleId);

        CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(userId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(roles,
                        Set.of(Role.LEADER_CIRCLE)));

        if (roles.contains(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            getCircleLeader(circle).getId(),
                            requestUser.getId()
                    ));
        }

        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.MEMBER)
                ))
                .consistOf(UserNotEqualValidator.of(
                        getCircleLeader(circle).getId(),
                        userId))
                .validate();

        return this.toCircleMemberResponseDto(
                updateCircleMemberStatus(circleMember.getId(), CircleMemberStatus.DROP),
                circle,
                user
        );
    }

    @Transactional
    public CircleMemberResponseDto acceptUser(User requestUser, String applicationId) {
        return this.updateUserApplication(
                requestUser,
                applicationId,
                CircleMemberStatus.MEMBER
        );
    }

    @Transactional
    public CircleMemberResponseDto rejectUser(User requestUser, String applicationId) {
        return updateUserApplication(
                requestUser,
                applicationId,
                CircleMemberStatus.REJECT
        );
    }

    private CircleMemberResponseDto updateUserApplication(
            User requestUser,
            String applicationId,
            CircleMemberStatus targetStatus
    ) {
        Set<Role> roles = requestUser.getRoles();

        CircleMember circleMember = circleMemberRepository.findById(applicationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_APPLY_NOT_FOUND
                )
        );

        User user = getUser(circleMember.getUser().getId());

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(roles,
                        Set.of(Role.LEADER_CIRCLE)));

        if (roles.contains(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            getCircleLeader(circleMember.getCircle()).getId(),
                            requestUser.getId()));
        }

        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.AWAIT)
                ))
                .validate();

        return this.toCircleMemberResponseDto(
                updateCircleMemberStatus(applicationId, targetStatus),
                circleMember.getCircle(),
                user
        );
    }

    @Transactional
    public CircleMemberResponseDto restoreUser(User loginUser, String circleId, String targetUserId) {
        Set<Role> roles = loginUser.getRoles();

        User targetUser = getUser(targetUserId);

        Circle circle = getCircle(circleId);

        CircleMember restoreTargetMember = circleMemberRepository.findByUser_IdAndCircle_Id(targetUserId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(loginUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(roles,
                        Set.of(Role.LEADER_CIRCLE)))
                .consistOf(UserEqualValidator.of(
                        loginUser.getId(),
                        getCircleLeader(circle).getId()
                ));
        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        restoreTargetMember.getStatus(),
                        List.of(CircleMemberStatus.DROP)
                )).validate();

        return this.toCircleMemberResponseDto(
                updateCircleMemberStatus(restoreTargetMember.getId(), CircleMemberStatus.MEMBER),
                circle,
                targetUser
        );
    }

    @Transactional(readOnly = true)
    public void exportCircleMembersToExcel(String circleId, HttpServletResponse response){
        Circle circle = getCircle(circleId);
        String circleName = circle.getName();

        List<ExportCircleMemberToExcelResponseDto> activeUserDtoList = circleMemberRepository.findByCircle_IdAndStatus(circleId, CircleMemberStatus.MEMBER)
                .stream()
                .map(circleMember -> {
                    User srcUser = circleMember.getUser();
                    UserCouncilFee userCouncilFee = userCouncilFeeRepository.findByUser(srcUser)
                            .orElseThrow(
                                    () -> new BadRequestException(
                                            ErrorCode.ROW_DOES_NOT_EXIST,
                                            MessageUtil.USER_COUNCIL_FEE_NOT_FOUND
                                    )
                            );
                    return this.toExportCircleMemberToExcelResponseDto(
                            srcUser,
                            userCouncilFee,
                            getRestOfSemester(userCouncilFee),
                            getIsAppliedCurrentSemester(userCouncilFee)
                    );
                }
                ).toList();
        List<ExportCircleMemberToExcelResponseDto> awaitingUserDtoList = circleMemberRepository.findByCircle_IdAndStatus(circleId, CircleMemberStatus.AWAIT)
                .stream()
                .map(circleMember -> {
                    User srcUser = circleMember.getUser();
                    UserCouncilFee userCouncilFee = userCouncilFeeRepository.findByUser(srcUser)
                            .orElseThrow(
                                    () -> new BadRequestException(
                                            ErrorCode.ROW_DOES_NOT_EXIST,
                                            MessageUtil.USER_COUNCIL_FEE_NOT_FOUND
                                    )
                            );
                    return this.toExportCircleMemberToExcelResponseDto(
                            srcUser,
                            userCouncilFee,
                            getRestOfSemester(userCouncilFee),
                            getIsAppliedCurrentSemester(userCouncilFee)
                    );
                }).toList();

        LinkedHashMap<String, List<ExportCircleMemberToExcelResponseDto>> sheetNameDataMap = new LinkedHashMap<>();
        sheetNameDataMap.put("활성 동아리원", activeUserDtoList);
        sheetNameDataMap.put("가입 대기 동아리원", awaitingUserDtoList);

        String fileName = circleName + "_부원명단";

        List<String> headerStringList = List.of(
                "아이디(이메일)",
                "이름",
                "닉네임",
                "입학년도",
                "학번",
                "학부/학과",
                "연락처",
                "학적 상태",
                "현재 등록 완료된 학기",
                "졸업 년도",
                "졸업 시기",
                "동문네트워크 가입일",
                "본 학기 학생회비 납부 여부",
                "학생회비 납부 시점",
                "학생회비 납부 차수",
                "적용 학생회비 학기",
                "잔여 학생회비 적용 학기",
                "학생회비 환불 여부"
        );

        circleExcelService.generateExcel(response, fileName, headerStringList, sheetNameDataMap);
    }

    @Transactional
    public void createApplicationForm(User writer, String circleId, LocalDateTime recruitEndDate, FormCreateRequestDto formCreateRequestDto) {
        Circle circle = getCircle(circleId);
        CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(writer.getId(), circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );

        ValidatorBucket.of()
                .consistOf(UserEqualValidator.of(
                        getCircleLeader(circle).getId(),
                        writer.getId()))
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.MEMBER)
                ))
                .validate();

        List<Form> priorFormList = formRepository.findAllByCircleAndIsDeleted(circle, false);

        if (!priorFormList.isEmpty()) {
            priorFormList.forEach(form -> {
                        form.setIsClosed(true);
                        form.setIsDeleted(true);
                    }
            );

            formRepository.saveAll(priorFormList);
        }

        formRepository.save(generateForm(formCreateRequestDto, circle));

        circle.setIsRecruit(true);
        circle.setRecruitEndDate(recruitEndDate);

        circleRepository.save(circle);
    }

    public Boolean isCircleApplicationFormExist(String circleId) {
        Circle circle = getCircle(circleId);

        return !formRepository.findAllByCircleAndIsDeletedAndIsClosed(circle, false, false).isEmpty();
    }

    public FormResponseDto getCircleApplicationForm(String circleId) {
        return this.toFormResponseDto(
                this.getForm(
                        getCircle(circleId)
                )
        );
    }

    public Page<FormResponseDto> getAllCircleApplicationFormList(User user, String circleId, Pageable pageable) {
        Circle circle = getCircle(circleId);

        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserEqualValidator.of(
                        getCircleLeader(circle).getId(),
                        user.getId()
                ))
                .validate();

        return formRepository.findAllByCircle(circle, pageable)
                .map(this::toFormResponseDto);
    }




    // Entity or Entity Information CRUD - Circle
    private Circle getCircle(String circleId) {
        return circleRepository.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );
    }

    private Optional<Circle> deleteCircle(String id) {
        return circleRepository.findById(id).map(
                srcCircle -> {
                    srcCircle.delete();
                    return circleRepository.save(srcCircle);
                }
        );
    }

    private Long getCircleNumMember(String circleId) {
        return circleMemberRepository.getNumMember(circleId);
    }

    // Entity or Entity Information CRUD - CircleMember
    private CircleMember updateCircleMemberStatus(String applicationId, CircleMemberStatus targetStatus) {
        return circleMemberRepository.findById(applicationId).map(
                circleMember -> {
                    circleMember.setStatus(targetStatus);
                    return circleMemberRepository.save(circleMember);
                }
        ).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        );
    }

    // Entity or Entity Information CRUD - User
    private User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );
    }

    private User getCircleLeader(Circle circle) {
        User leader = circle.getLeader().orElse(null);

        if (leader == null) {
            throw new InternalServerException(
                    ErrorCode.INTERNAL_SERVER,
                    MessageUtil.CIRCLE_WITHOUT_LEADER
            );
        }

        return userRepository.findById(leader.getId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );
    }

    private User updateRole(User targetUser, Role newRole) {
        Set<Role> roles = targetUser.getRoles();

        //common이 포함되어 있을때는 common을 지우고 새로운 역할 추가
        if(roles.contains(Role.COMMON)){
            roles.remove(Role.COMMON);
        }
        roles.add(newRole);
        return this.userRepository.save(targetUser);
    }

    private User removeRole(User targetUser, Role targetRole) {
        Set<Role> roles = targetUser.getRoles();

        if(roles.contains(targetRole)){
            roles.remove(targetRole);
            //TODO: 디폴트로 common이라는 역할을 남기는 경우를 생성하고 지우기
            roles.add(Role.COMMON);
        }
        targetUser.setRoles(roles);

        return this.userRepository.save(targetUser);
    }

    private Form getForm(Circle circle) {
        List<Form> formList = formRepository.findAllByCircleAndIsDeletedAndIsClosed(circle, false, false);

        if (formList.size() != 1) {
            throw new InternalServerException(
                    ErrorCode.INTERNAL_SERVER,
                    MessageUtil.INTERNAL_SERVER_ERROR
            );
        }

        return formList.get(0);
    }

    // Entity or Entity Information CRUD - Board
    private List<Board> deleteAllCircleBoard(String circleId) {
        List<Board> boardList = boardRepository.findByCircle_IdAndIsDeletedIsFalseOrderByCreatedAtAsc(circleId);
        for (Board board : boardList) {
            boardRepository.findById(board.getId()).map(
                    srcBoard -> {
                        srcBoard.setIsDeleted(true);

                        return boardRepository.save(srcBoard);
                    }).orElseThrow(
                    () -> new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            MessageUtil.INTERNAL_SERVER_ERROR
                    ));
        }
        return boardList;
    }

    private Form generateForm(FormCreateRequestDto formCreateRequestDto, Circle circle) {
        AtomicReference<Integer> questionNumber = new AtomicReference<>(1);
        List<FormQuestion> formQuestionList = Optional.ofNullable(formCreateRequestDto.getQuestionCreateRequestDtoList())
                .orElse(new ArrayList<>())
                .stream().map(questionCreateRequestDto -> {

                    AtomicReference<Integer> optionNumber = new AtomicReference<>(1);

                    List<FormQuestionOption> formQuestionOptionList = Optional.ofNullable(questionCreateRequestDto.getOptionCreateRequestDtoList())
                            .orElse(new ArrayList<>())
                            .stream()
                            .map(optionDto -> FormQuestionOption.of(
                                    optionNumber.getAndSet(optionNumber.get() + 1),
                                    optionDto.getOptionText(),
                                    null
                            )).toList();

                    if (questionCreateRequestDto.getQuestionType().equals(QuestionType.OBJECTIVE)) {
                        if (questionCreateRequestDto.getIsMultiple() == null ||
                                questionCreateRequestDto.getOptionCreateRequestDtoList().isEmpty()
                        ) {
                            throw new BadRequestException(
                                    ErrorCode.INVALID_PARAMETER,
                                    MessageUtil.INVALID_QUESTION_INFO
                            );
                        }
                    } else {
                        if (questionCreateRequestDto.getIsMultiple() != null ||
                                !questionCreateRequestDto.getOptionCreateRequestDtoList().isEmpty()
                        ) {
                            throw new BadRequestException(
                                    ErrorCode.INVALID_PARAMETER,
                                    MessageUtil.INVALID_QUESTION_INFO
                            );
                        }
                    }

                    FormQuestion formQuestion = FormQuestion.of(
                            questionNumber.getAndSet(questionNumber.get() + 1),
                            questionCreateRequestDto.getQuestionType(),
                            questionCreateRequestDto.getQuestionText(),
                            questionCreateRequestDto.getIsMultiple(),
                            formQuestionOptionList,
                            null
                    );

                    formQuestionOptionList.forEach(option -> option.setFormQuestion(formQuestion));

                    return formQuestion;
                }).toList();

        Form form = Form.createCircleApplicationForm(
                formCreateRequestDto.getTitle(),
                formQuestionList,
                circle,
                formCreateRequestDto.getIsAllowedEnrolled(),
                formCreateRequestDto.getIsAllowedEnrolled() ?
                        RegisteredSemesterManager.fromEnumList(
                                formCreateRequestDto.getEnrolledRegisteredSemesterList()
                        )
                        : null,
                formCreateRequestDto.getIsAllowedEnrolled() ?
                        formCreateRequestDto.getIsNeedCouncilFeePaid()
                        : false,
                formCreateRequestDto.getIsAllowedLeaveOfAbsence(),
                formCreateRequestDto.getIsAllowedLeaveOfAbsence() ?
                        RegisteredSemesterManager.fromEnumList(
                                formCreateRequestDto.getLeaveOfAbsenceRegisteredSemesterList()
                        )
                        : null,
                formCreateRequestDto.getIsAllowedGraduation()
        );

        formQuestionList.forEach(question -> question.setForm(form));

        return form;
    }

    public Reply replyForm(
            Form form,
            FormReplyRequestDto formReplyRequestDto,
            User writer
    ) {
        if (form.getIsClosed()) {
            throw new BadRequestException(
                    ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                    MessageUtil.FORM_CLOSED
            );
        }

        this.validateToReply(writer, form);

        // 주관식, 객관식 질문에 따라 유효한 답변인지 검증 및 저장
        List<ReplyQuestion> replyQuestionList = new ArrayList<>();

        for (QuestionReplyRequestDto questionReplyRequestDto : formReplyRequestDto.getQuestionReplyRequestDtoList()) {
            FormQuestion formQuestion = getQuestion(questionReplyRequestDto.getQuestionId());

            // 객관식일 시
            if (formQuestion.getQuestionType().equals(QuestionType.OBJECTIVE)) {
                if (questionReplyRequestDto.getQuestionReply() != null) {
                    throw new BadRequestException(
                            ErrorCode.INVALID_PARAMETER,
                            MessageUtil.INVALID_REPLY_INFO
                    );
                }

                if (!formQuestion.getIsMultiple() && questionReplyRequestDto.getSelectedOptionList().size() > 1) {
                    throw new BadRequestException(
                            ErrorCode.INVALID_PARAMETER,
                            MessageUtil.INVALID_REPLY_INFO
                    );
                }

                // 객관식일 시: 유효한 옵션 번호 선택했는지 검사
                List<Integer> formQuestionOptionNumberList = formQuestion.getFormQuestionOptionList()
                        .stream()
                        .map(FormQuestionOption::getNumber)
                        .toList();

                questionReplyRequestDto.getSelectedOptionList().forEach(optionNumber -> {
                    if (!formQuestionOptionNumberList.contains(optionNumber)) {
                        throw new BadRequestException(
                                ErrorCode.INVALID_PARAMETER,
                                MessageUtil.INVALID_REPLY_INFO
                        );
                    }
                });
            }
            // 주관식일 시
            else {
                if (questionReplyRequestDto.getSelectedOptionList() != null) {
                    throw new BadRequestException(
                            ErrorCode.INVALID_PARAMETER,
                            MessageUtil.INVALID_REPLY_INFO
                    );
                }
            }

            ReplyQuestion replyQuestion = ReplyQuestion.of(
                    formQuestion,
                    formQuestion.getQuestionType().equals(QuestionType.SUBJECTIVE) ?
                            questionReplyRequestDto.getQuestionReply()
                            : null,
                    formQuestion.getQuestionType().equals(QuestionType.OBJECTIVE) ?
                            questionReplyRequestDto.getSelectedOptionList()
                            : null
            );

            replyQuestionList.add(replyQuestion);
        }

        // 답변 개수 맞는지 확인
        if (replyQuestionList.size() != form.getFormQuestionList().size()) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    MessageUtil.REPLY_SIZE_INVALID
            );
        }

        // 모든 문항에 대해 답변 정확히 하나 있는지 검사(답변 유효성 검사)
        form.getFormQuestionList().forEach(formQuestion -> {
            int questionReplyCount = 0;
            for (ReplyQuestion replyQuestion : replyQuestionList) {
                if (formQuestion.equals(replyQuestion.getFormQuestion())) {
                    questionReplyCount++;
                }
            }
            if (questionReplyCount != 1) {
                throw new BadRequestException(
                        ErrorCode.INVALID_PARAMETER,
                        MessageUtil.INVALID_REPLY_INFO
                );
            }
        });

        Reply reply = Reply.of(form, writer, replyQuestionList);

        replyQuestionList.forEach(replyQuestion -> replyQuestion.setReply(reply));

        return reply;
    }


    // writer가 해당 form에 대한 권한이 있는지 확인
    private void validateToReply(User writer, Form form) {
        Set<AcademicStatus> allowedAcademicStatus = new HashSet<>();

        if (form.getIsAllowedEnrolled())
            allowedAcademicStatus.add(AcademicStatus.ENROLLED);
        if (form.getIsAllowedLeaveOfAbsence())
            allowedAcademicStatus.add(AcademicStatus.LEAVE_OF_ABSENCE);
        if (form.getIsAllowedGraduation())
            allowedAcademicStatus.add(AcademicStatus.GRADUATED);

        if (!allowedAcademicStatus.contains(writer.getAcademicStatus())) {
            throw new BadRequestException(
                    ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                    MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
            );
        } else {
            if (allowedAcademicStatus.contains(AcademicStatus.ENROLLED)
                    && writer.getAcademicStatus().equals(AcademicStatus.ENROLLED)
            ) {
                EnumSet<RegisteredSemester> allowedRegisteredSemester = form.getEnrolledRegisteredSemester();
                if (!allowedRegisteredSemester
                        .stream()
                        .map(RegisteredSemester::getSemester)
                        .collect(Collectors.toSet())
                        .contains(writer.getCurrentCompletedSemester())
                ) {
                    throw new BadRequestException(
                            ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                            MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
                    );
                }

                if (form.getIsNeedCouncilFeePaid()) {
                    // 학생회비 납부 필요
                    UserCouncilFee userCouncilFee = userCouncilFeeRepository.findByUser(writer).orElseThrow(
                            () -> new BadRequestException(
                                    ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                                    MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
                            )
                    );

                    if (!getIsAppliedCurrentSemester(userCouncilFee)) {
                        throw new BadRequestException(
                                ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                                MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
                        );
                    }
                }
            }

            if (allowedAcademicStatus.contains(AcademicStatus.LEAVE_OF_ABSENCE)
                    && writer.getAcademicStatus().equals(AcademicStatus.LEAVE_OF_ABSENCE)
            ) {
                EnumSet<RegisteredSemester> allowedRegisteredSemester = form.getLeaveOfAbsenceRegisteredSemester();
                if (!allowedRegisteredSemester
                        .stream()
                        .map(RegisteredSemester::getSemester)
                        .collect(Collectors.toSet())
                        .contains(writer.getCurrentCompletedSemester())
                ) {
                    throw new BadRequestException(
                            ErrorCode.NOT_ALLOWED_TO_REPLY_FORM,
                            MessageUtil.NOT_ALLOWED_TO_REPLY_FORM
                    );
                }
            }
        }
    }

    private FormQuestion getQuestion(String questionId){
        return questionRepository.findById(questionId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.QUESTION_NOT_FOUND
                )
        );
    }

    // ValidatorBucket Constructor

    private ValidatorBucket initializeValidator(Boolean isDeleted, String staticValue) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(isDeleted, staticValue));
        return validatorBucket;
    }

    private ValidatorBucket initializeUserValidator(UserState state, Set<Role> roles) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(state))
                .consistOf(UserRoleIsNoneValidator.of(roles));
        return validatorBucket;
    }

    // Private method
    private Integer getRestOfSemester(UserCouncilFee userCouncilFee) {
        Integer startOfAppliedSemester = userCouncilFee.getPaidAt();
        Integer endOfAppliedSemester = ( userCouncilFee.getIsRefunded() ) ?
                ( startOfAppliedSemester - 1 ) + userCouncilFee.getNumOfPaidSemester() :
                userCouncilFee.getRefundedAt();
        Integer restOfSemester;

        if (userCouncilFee.getIsJoinedService()) {
            restOfSemester = Math.max(endOfAppliedSemester - userCouncilFee.getUser().getCurrentCompletedSemester(), 0);
        } else {
            restOfSemester = Math.max(endOfAppliedSemester - userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester(), 0);
        }
        return restOfSemester;
    }

    private Boolean getIsAppliedCurrentSemester(UserCouncilFee userCouncilFee) {
        Integer startOfAppliedSemester = userCouncilFee.getPaidAt();
        Integer endOfAppliedSemester = ( userCouncilFee.getIsRefunded() ) ?
                ( startOfAppliedSemester - 1 ) + userCouncilFee.getNumOfPaidSemester() :
                userCouncilFee.getRefundedAt();
        Boolean isAppliedThisSemester;

        if (userCouncilFee.getIsJoinedService()) {
            isAppliedThisSemester = (startOfAppliedSemester <= userCouncilFee.getUser().getCurrentCompletedSemester()) &&
                    (userCouncilFee.getUser().getCurrentCompletedSemester() <= endOfAppliedSemester);
        } else {
            isAppliedThisSemester = (startOfAppliedSemester <= userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester()) &&
                    (userCouncilFee.getCouncilFeeFakeUser().getCurrentCompletedSemester() <= endOfAppliedSemester);
        }
        return isAppliedThisSemester;
    }

    // Dto Mapper

    private UserResponseDto toUserResponseDto(User user) {
        return CircleDtoMapper.INSTANCE.toUserResponseDto(user);
    }

    private CircleResponseDto toCircleResponseDto(Circle circle) {
        return CircleDtoMapper.INSTANCE.toCircleResponseDto(circle);
    }

    private CircleResponseDto toCircleResponseDtoExtended(Circle circle, Long numMember) {
        return CircleDtoMapper.INSTANCE.toCircleResponseDtoExtended(circle, numMember);
    }

    private CirclesResponseDto toCirclesResponseDto(Circle circle, Long numMember) {
        return CircleDtoMapper.INSTANCE.toCirclesResponseDto(circle, numMember);
    }

    private CirclesResponseDto toCirclesResponseDtoExtended(Circle circle, Long numMember, LocalDateTime joinedAt) {
        return CircleDtoMapper.INSTANCE.toCirclesResponseDtoExtended(circle, numMember, joinedAt);
    }

    private BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board board, Set<Role> userRoles) {
        return CircleDtoMapper.INSTANCE.toBoardOfCircleResponseDto(
                board,
                isWriteable(board, userRoles)
        );
    }

    private BoardOfCircleResponseDto toBoardOfCircleResponseDtoExtended(Board board, Set<Role> userRoles, Post post, Long numComment) {
        return CircleDtoMapper.INSTANCE.toBoardOfCircleResponseDtoExtended(
                board,
                isWriteable(board, userRoles),
                post,
                numComment
        );
    }

    private CircleBoardsResponseDto toCircleBoardsResponseDto(Circle circle, Long numMember, List<BoardOfCircleResponseDto> boardList) {
        return CircleDtoMapper.INSTANCE.toCircleBoardsResponseDto(this.toCircleResponseDtoExtended(circle, numMember), boardList);
    }

    private CircleMemberResponseDto toCircleMemberResponseDto(CircleMember circleMember, CircleResponseDto circleResponseDto, UserResponseDto userResponseDto) {
        return CircleDtoMapper.INSTANCE.toCircleMemberResponseDto(circleMember, circleResponseDto, userResponseDto);
    }

    private CircleMemberResponseDto toCircleMemberResponseDto(CircleMember circleMember, Circle circle, User user) {
        return this.toCircleMemberResponseDto(
                circleMember,
                this.toCircleResponseDto(circle),
                this.toUserResponseDto(user)
        );
    }

    private DuplicatedCheckResponseDto toDuplicatedCheckResponseDto(Boolean isDuplicated) {
        return CircleDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(isDuplicated);
    }

    private ExportCircleMemberToExcelResponseDto toExportCircleMemberToExcelResponseDto(User user, UserCouncilFee userCouncilFee, Integer restOfSemester, Boolean isAppliedThisSemester){
        return CircleDtoMapper.INSTANCE.toExportCircleMemberToExcelResponseDto(user, userCouncilFee, restOfSemester, isAppliedThisSemester, userCouncilFee.getNumOfPaidSemester() - restOfSemester);
    }

    private FormResponseDto toFormResponseDto(Form form) {
        return FormDtoMapper.INSTANCE.toFormResponseDto(
                form,
                form.getFormQuestionList().stream()
                        .map(this::toQuestionResponseDto)
                        .collect(Collectors.toList())
        );
    }

    private QuestionResponseDto toQuestionResponseDto(FormQuestion formQuestion) {
        return FormDtoMapper.INSTANCE.toQuestionResponseDto(
                formQuestion,
                formQuestion.getFormQuestionOptionList().stream()
                        .map(this::toOptionResponseDto)
                        .collect(Collectors.toList())
        );
    }

    private OptionResponseDto toOptionResponseDto(FormQuestionOption formQuestionOption) {
        return FormDtoMapper.INSTANCE.toOptionResponseDto(formQuestionOption);
    }

}
