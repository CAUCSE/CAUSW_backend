package net.causw.application.dto.util.dtoMapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import net.causw.adapter.persistence.semester.Semester;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.adapter.persistence.userAcademicRecord.UserAcademicRecordLog;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.userAcademicRecordApplication.CurrentUserAcademicRecordApplicationResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.CurrentUserAcademicRecordApplicationResponseDto.CurrentUserAcademicRecordApplicationResponseDtoBuilder;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordApplicationInfoResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordApplicationInfoResponseDto.UserAcademicRecordApplicationInfoResponseDtoBuilder;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordApplicationListResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordApplicationListResponseDto.UserAcademicRecordApplicationListResponseDtoBuilder;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordApplicationResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordApplicationResponseDto.UserAcademicRecordApplicationResponseDtoBuilder;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordInfoResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordInfoResponseDto.UserAcademicRecordInfoResponseDtoBuilder;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordListResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.UserAcademicRecordListResponseDto.UserAcademicRecordListResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-13T05:58:23+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.12 (Azul Systems, Inc.)"
)
@Component
public class UserAcademicRecordDtoMapperImpl implements UserAcademicRecordDtoMapper {

    @Override
    public UserAcademicRecordListResponseDto toUserAcademicRecordListResponseDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserAcademicRecordListResponseDtoBuilder userAcademicRecordListResponseDto = UserAcademicRecordListResponseDto.builder();

        userAcademicRecordListResponseDto.userId( user.getId() );
        userAcademicRecordListResponseDto.userName( user.getName() );
        userAcademicRecordListResponseDto.studentId( user.getStudentId() );

        return userAcademicRecordListResponseDto.build();
    }

    @Override
    public UserAcademicRecordApplicationListResponseDto toUserAcademicRecordApplicationListResponseDto(UserAcademicRecordApplication userAcademicRecordApplication) {
        if ( userAcademicRecordApplication == null ) {
            return null;
        }

        UserAcademicRecordApplicationListResponseDtoBuilder userAcademicRecordApplicationListResponseDto = UserAcademicRecordApplicationListResponseDto.builder();

        userAcademicRecordApplicationListResponseDto.userId( userAcademicRecordApplicationUserId( userAcademicRecordApplication ) );
        userAcademicRecordApplicationListResponseDto.userName( userAcademicRecordApplicationUserName( userAcademicRecordApplication ) );
        userAcademicRecordApplicationListResponseDto.studentId( userAcademicRecordApplicationUserStudentId( userAcademicRecordApplication ) );
        userAcademicRecordApplicationListResponseDto.userAcademicRecordApplicationId( userAcademicRecordApplication.getId() );

        return userAcademicRecordApplicationListResponseDto.build();
    }

    @Override
    public UserAcademicRecordInfoResponseDto toUserAcademicRecordInfoResponseDto(User user, List<UserAcademicRecordApplicationResponseDto> userAcademicRecordApplicationResponseDtoList) {
        if ( user == null && userAcademicRecordApplicationResponseDtoList == null ) {
            return null;
        }

        UserAcademicRecordInfoResponseDtoBuilder userAcademicRecordInfoResponseDto = UserAcademicRecordInfoResponseDto.builder();

        if ( user != null ) {
            userAcademicRecordInfoResponseDto.userId( user.getId() );
            userAcademicRecordInfoResponseDto.userName( user.getName() );
            userAcademicRecordInfoResponseDto.studentId( user.getStudentId() );
            userAcademicRecordInfoResponseDto.academicStatus( user.getAcademicStatus() );
            userAcademicRecordInfoResponseDto.currentCompleteSemester( user.getCurrentCompletedSemester() );
            userAcademicRecordInfoResponseDto.note( user.getAcademicStatusNote() );
        }
        if ( userAcademicRecordApplicationResponseDtoList != null ) {
            List<UserAcademicRecordApplicationResponseDto> list = userAcademicRecordApplicationResponseDtoList;
            if ( list != null ) {
                userAcademicRecordInfoResponseDto.userAcademicRecordApplicationResponseDtoList( new ArrayList<UserAcademicRecordApplicationResponseDto>( list ) );
            }
        }

        return userAcademicRecordInfoResponseDto.build();
    }

    @Override
    public UserAcademicRecordApplicationResponseDto toUserAcademicRecordApplicationResponseDto(UserAcademicRecordLog userAcademicRecordLog) {
        if ( userAcademicRecordLog == null ) {
            return null;
        }

        UserAcademicRecordApplicationResponseDtoBuilder userAcademicRecordApplicationResponseDto = UserAcademicRecordApplicationResponseDto.builder();

        if ( userAcademicRecordLog.getTargetAcademicRecordStatus() != null ) {
            userAcademicRecordApplicationResponseDto.targetAcademicStatus( userAcademicRecordLog.getTargetAcademicRecordStatus().name() );
        }
        userAcademicRecordApplicationResponseDto.userNote( userAcademicRecordLog.getNote() );
        List<UuidFile> userAcademicRecordAttachImageUuidFileList = userAcademicRecordLogTargetUserAcademicRecordApplicationUserAcademicRecordAttachImageUuidFileList( userAcademicRecordLog );
        userAcademicRecordApplicationResponseDto.attachedImageUrlList( mapUuidFileListToFileUrlList( userAcademicRecordAttachImageUuidFileList ) );
        userAcademicRecordApplicationResponseDto.changeDate( userAcademicRecordLog.getUpdatedAt() );

        return userAcademicRecordApplicationResponseDto.build();
    }

    @Override
    public UserAcademicRecordApplicationInfoResponseDto toUserAcademicRecordApplicationInfoResponseDto(UserAcademicRecordApplication userAcademicRecordApplication) {
        if ( userAcademicRecordApplication == null ) {
            return null;
        }

        UserAcademicRecordApplicationInfoResponseDtoBuilder userAcademicRecordApplicationInfoResponseDto = UserAcademicRecordApplicationInfoResponseDto.builder();

        userAcademicRecordApplicationInfoResponseDto.userId( userAcademicRecordApplicationUserId( userAcademicRecordApplication ) );
        userAcademicRecordApplicationInfoResponseDto.userName( userAcademicRecordApplicationUserName( userAcademicRecordApplication ) );
        userAcademicRecordApplicationInfoResponseDto.studentId( userAcademicRecordApplicationUserStudentId( userAcademicRecordApplication ) );
        userAcademicRecordApplicationInfoResponseDto.academicRecordRequestStatus( userAcademicRecordApplication.getAcademicRecordRequestStatus() );
        userAcademicRecordApplicationInfoResponseDto.targetAcademicStatus( userAcademicRecordApplication.getTargetAcademicStatus() );
        userAcademicRecordApplicationInfoResponseDto.targetCompletedSemester( userAcademicRecordApplication.getTargetCompletedSemester() );
        userAcademicRecordApplicationInfoResponseDto.note( userAcademicRecordApplication.getNote() );
        userAcademicRecordApplicationInfoResponseDto.attachedImageUrlList( mapUuidFileListToFileUrlList( userAcademicRecordApplication.getUserAcademicRecordAttachImageUuidFileList() ) );
        userAcademicRecordApplicationInfoResponseDto.rejectMessage( userAcademicRecordApplication.getRejectMessage() );

        return userAcademicRecordApplicationInfoResponseDto.build();
    }

    @Override
    public CurrentUserAcademicRecordApplicationResponseDto toCurrentUserAcademicRecordResponseDto(Semester semester, UserAcademicRecordApplication userAcademicRecordApplication, Boolean isRejected) {
        if ( semester == null && userAcademicRecordApplication == null && isRejected == null ) {
            return null;
        }

        CurrentUserAcademicRecordApplicationResponseDtoBuilder currentUserAcademicRecordApplicationResponseDto = CurrentUserAcademicRecordApplicationResponseDto.builder();

        if ( semester != null ) {
            currentUserAcademicRecordApplicationResponseDto.currentSemesterYear( semester.getSemesterYear() );
            currentUserAcademicRecordApplicationResponseDto.currentSemesterType( semester.getSemesterType() );
        }
        if ( userAcademicRecordApplication != null ) {
            currentUserAcademicRecordApplicationResponseDto.rejectMessage( userAcademicRecordApplication.getRejectMessage() );
            if ( userAcademicRecordApplication.getTargetAcademicStatus() != null ) {
                currentUserAcademicRecordApplicationResponseDto.targetAcademicStatus( userAcademicRecordApplication.getTargetAcademicStatus().name() );
            }
            currentUserAcademicRecordApplicationResponseDto.targetCompletedSemester( userAcademicRecordApplication.getTargetCompletedSemester() );
            currentUserAcademicRecordApplicationResponseDto.userNote( userAcademicRecordApplication.getNote() );
            currentUserAcademicRecordApplicationResponseDto.attachedImageUrlList( mapUuidFileListToFileUrlList( userAcademicRecordApplication.getUserAcademicRecordAttachImageUuidFileList() ) );
        }
        if ( isRejected != null ) {
            currentUserAcademicRecordApplicationResponseDto.isRejected( isRejected );
        }

        return currentUserAcademicRecordApplicationResponseDto.build();
    }

    private String userAcademicRecordApplicationUserId(UserAcademicRecordApplication userAcademicRecordApplication) {
        if ( userAcademicRecordApplication == null ) {
            return null;
        }
        User user = userAcademicRecordApplication.getUser();
        if ( user == null ) {
            return null;
        }
        String id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String userAcademicRecordApplicationUserName(UserAcademicRecordApplication userAcademicRecordApplication) {
        if ( userAcademicRecordApplication == null ) {
            return null;
        }
        User user = userAcademicRecordApplication.getUser();
        if ( user == null ) {
            return null;
        }
        String name = user.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String userAcademicRecordApplicationUserStudentId(UserAcademicRecordApplication userAcademicRecordApplication) {
        if ( userAcademicRecordApplication == null ) {
            return null;
        }
        User user = userAcademicRecordApplication.getUser();
        if ( user == null ) {
            return null;
        }
        String studentId = user.getStudentId();
        if ( studentId == null ) {
            return null;
        }
        return studentId;
    }

    private List<UuidFile> userAcademicRecordLogTargetUserAcademicRecordApplicationUserAcademicRecordAttachImageUuidFileList(UserAcademicRecordLog userAcademicRecordLog) {
        if ( userAcademicRecordLog == null ) {
            return null;
        }
        UserAcademicRecordApplication targetUserAcademicRecordApplication = userAcademicRecordLog.getTargetUserAcademicRecordApplication();
        if ( targetUserAcademicRecordApplication == null ) {
            return null;
        }
        List<UuidFile> userAcademicRecordAttachImageUuidFileList = targetUserAcademicRecordApplication.getUserAcademicRecordAttachImageUuidFileList();
        if ( userAcademicRecordAttachImageUuidFileList == null ) {
            return null;
        }
        return userAcademicRecordAttachImageUuidFileList;
    }
}
