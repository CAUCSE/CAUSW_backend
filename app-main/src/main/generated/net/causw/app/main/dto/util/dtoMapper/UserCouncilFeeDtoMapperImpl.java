package net.causw.app.main.dto.util.dtoMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import javax.annotation.processing.Generated;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.userCouncilFee.CouncilFeeFakeUser;
import net.causw.app.main.domain.model.entity.userCouncilFee.UserCouncilFee;
import net.causw.app.main.dto.userCouncilFee.CurrentUserCouncilFeeResponseDto;
import net.causw.app.main.dto.userCouncilFee.CurrentUserCouncilFeeResponseDto.CurrentUserCouncilFeeResponseDtoBuilder;
import net.causw.app.main.dto.userCouncilFee.UserCouncilFeeListResponseDto;
import net.causw.app.main.dto.userCouncilFee.UserCouncilFeeListResponseDto.UserCouncilFeeListResponseDtoBuilder;
import net.causw.app.main.dto.userCouncilFee.UserCouncilFeeResponseDto;
import net.causw.app.main.dto.userCouncilFee.UserCouncilFeeResponseDto.UserCouncilFeeResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:46+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
@Component
public class UserCouncilFeeDtoMapperImpl implements UserCouncilFeeDtoMapper {

    private final DatatypeFactory datatypeFactory;

    public UserCouncilFeeDtoMapperImpl() {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        }
        catch ( DatatypeConfigurationException ex ) {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public UserCouncilFeeListResponseDto toUserCouncilFeeListResponseDto(UserCouncilFee userCouncilFee, User user) {
        if ( userCouncilFee == null && user == null ) {
            return null;
        }

        UserCouncilFeeListResponseDtoBuilder userCouncilFeeListResponseDto = UserCouncilFeeListResponseDto.builder();

        if ( userCouncilFee != null ) {
            userCouncilFeeListResponseDto.userCouncilFeeId( userCouncilFee.getId() );
            userCouncilFeeListResponseDto.isJoinedService( userCouncilFee.getIsJoinedService() );
        }
        if ( user != null ) {
            userCouncilFeeListResponseDto.userId( user.getId() );
            userCouncilFeeListResponseDto.userName( user.getName() );
            userCouncilFeeListResponseDto.studentId( user.getStudentId() );
        }

        return userCouncilFeeListResponseDto.build();
    }

    @Override
    public UserCouncilFeeListResponseDto toUserCouncilFeeListResponseDtoReduced(UserCouncilFee userCouncilFee, CouncilFeeFakeUser councilFeeFakeUser) {
        if ( userCouncilFee == null && councilFeeFakeUser == null ) {
            return null;
        }

        UserCouncilFeeListResponseDtoBuilder userCouncilFeeListResponseDto = UserCouncilFeeListResponseDto.builder();

        if ( userCouncilFee != null ) {
            userCouncilFeeListResponseDto.userCouncilFeeId( userCouncilFee.getId() );
            userCouncilFeeListResponseDto.isJoinedService( userCouncilFee.getIsJoinedService() );
        }
        if ( councilFeeFakeUser != null ) {
            userCouncilFeeListResponseDto.councilFeeFakeUserId( councilFeeFakeUser.getId() );
            userCouncilFeeListResponseDto.userName( councilFeeFakeUser.getName() );
            userCouncilFeeListResponseDto.studentId( councilFeeFakeUser.getStudentId() );
        }

        return userCouncilFeeListResponseDto.build();
    }

    @Override
    public UserCouncilFeeResponseDto toUserCouncilFeeResponseDto(UserCouncilFee userCouncilFee, User user, Integer restOfSemester, Boolean isAppliedThisSemester) {
        if ( userCouncilFee == null && user == null && restOfSemester == null && isAppliedThisSemester == null ) {
            return null;
        }

        UserCouncilFeeResponseDtoBuilder userCouncilFeeResponseDto = UserCouncilFeeResponseDto.builder();

        if ( userCouncilFee != null ) {
            userCouncilFeeResponseDto.userCouncilFeeId( userCouncilFee.getId() );
            userCouncilFeeResponseDto.isJoinedService( userCouncilFee.getIsJoinedService() );
            userCouncilFeeResponseDto.paidAt( userCouncilFee.getPaidAt() );
            userCouncilFeeResponseDto.numOfPaidSemester( userCouncilFee.getNumOfPaidSemester() );
            userCouncilFeeResponseDto.isRefunded( userCouncilFee.getIsRefunded() );
            userCouncilFeeResponseDto.refundedAt( userCouncilFee.getRefundedAt() );
        }
        if ( user != null ) {
            userCouncilFeeResponseDto.userId( user.getId() );
            userCouncilFeeResponseDto.email( user.getEmail() );
            userCouncilFeeResponseDto.userName( user.getName() );
            userCouncilFeeResponseDto.studentId( user.getStudentId() );
            userCouncilFeeResponseDto.admissionYear( user.getAdmissionYear() );
            userCouncilFeeResponseDto.nickname( user.getNickname() );
            userCouncilFeeResponseDto.major( user.getMajor() );
            userCouncilFeeResponseDto.academicStatus( user.getAcademicStatus() );
            userCouncilFeeResponseDto.currentCompletedSemester( user.getCurrentCompletedSemester() );
            userCouncilFeeResponseDto.graduationYear( user.getGraduationYear() );
            userCouncilFeeResponseDto.graduationType( user.getGraduationType() );
            userCouncilFeeResponseDto.phoneNumber( user.getPhoneNumber() );
            userCouncilFeeResponseDto.joinedAt( xmlGregorianCalendarToLocalDate( localDateTimeToXmlGregorianCalendar( user.getCreatedAt() ) ) );
        }
        if ( restOfSemester != null ) {
            userCouncilFeeResponseDto.restOfSemester( restOfSemester );
        }
        if ( isAppliedThisSemester != null ) {
            userCouncilFeeResponseDto.isAppliedThisSemester( isAppliedThisSemester );
        }

        return userCouncilFeeResponseDto.build();
    }

    @Override
    public UserCouncilFeeResponseDto toUserCouncilFeeResponseDtoReduced(UserCouncilFee userCouncilFee, CouncilFeeFakeUser councilFeeFakeUser, Integer restOfSemester, Boolean isAppliedThisSemester) {
        if ( userCouncilFee == null && councilFeeFakeUser == null && restOfSemester == null && isAppliedThisSemester == null ) {
            return null;
        }

        UserCouncilFeeResponseDtoBuilder userCouncilFeeResponseDto = UserCouncilFeeResponseDto.builder();

        if ( userCouncilFee != null ) {
            userCouncilFeeResponseDto.userCouncilFeeId( userCouncilFee.getId() );
            userCouncilFeeResponseDto.isJoinedService( userCouncilFee.getIsJoinedService() );
            userCouncilFeeResponseDto.paidAt( userCouncilFee.getPaidAt() );
            userCouncilFeeResponseDto.numOfPaidSemester( userCouncilFee.getNumOfPaidSemester() );
            userCouncilFeeResponseDto.isRefunded( userCouncilFee.getIsRefunded() );
            userCouncilFeeResponseDto.refundedAt( userCouncilFee.getRefundedAt() );
        }
        if ( councilFeeFakeUser != null ) {
            userCouncilFeeResponseDto.councilFeeFakeUserId( councilFeeFakeUser.getId() );
            userCouncilFeeResponseDto.userName( councilFeeFakeUser.getName() );
            userCouncilFeeResponseDto.studentId( councilFeeFakeUser.getStudentId() );
            userCouncilFeeResponseDto.admissionYear( councilFeeFakeUser.getAdmissionYear() );
            userCouncilFeeResponseDto.major( councilFeeFakeUser.getMajor() );
            userCouncilFeeResponseDto.academicStatus( councilFeeFakeUser.getAcademicStatus() );
            userCouncilFeeResponseDto.currentCompletedSemester( councilFeeFakeUser.getCurrentCompletedSemester() );
            userCouncilFeeResponseDto.graduationYear( councilFeeFakeUser.getGraduationYear() );
            userCouncilFeeResponseDto.graduationType( councilFeeFakeUser.getGraduationType() );
            userCouncilFeeResponseDto.phoneNumber( councilFeeFakeUser.getPhoneNumber() );
        }
        if ( restOfSemester != null ) {
            userCouncilFeeResponseDto.restOfSemester( restOfSemester );
        }
        if ( isAppliedThisSemester != null ) {
            userCouncilFeeResponseDto.isAppliedThisSemester( isAppliedThisSemester );
        }

        return userCouncilFeeResponseDto.build();
    }

    @Override
    public CurrentUserCouncilFeeResponseDto toCurrentUserCouncilFeeResponseDto(UserCouncilFee userCouncilFee, Integer restOfSemester, Boolean isAppliedThisSemester) {
        if ( userCouncilFee == null && restOfSemester == null && isAppliedThisSemester == null ) {
            return null;
        }

        CurrentUserCouncilFeeResponseDtoBuilder currentUserCouncilFeeResponseDto = CurrentUserCouncilFeeResponseDto.builder();

        if ( userCouncilFee != null ) {
            currentUserCouncilFeeResponseDto.isRefunded( userCouncilFee.getIsRefunded() );
            currentUserCouncilFeeResponseDto.numOfPaidSemester( userCouncilFee.getNumOfPaidSemester() );
        }
        if ( restOfSemester != null ) {
            currentUserCouncilFeeResponseDto.restOfSemester( restOfSemester );
        }
        if ( isAppliedThisSemester != null ) {
            currentUserCouncilFeeResponseDto.isAppliedThisSemester( isAppliedThisSemester );
        }

        return currentUserCouncilFeeResponseDto.build();
    }

    private static LocalDate xmlGregorianCalendarToLocalDate( XMLGregorianCalendar xcal ) {
        if ( xcal == null ) {
            return null;
        }

        return LocalDate.of( xcal.getYear(), xcal.getMonth(), xcal.getDay() );
    }

    private XMLGregorianCalendar localDateTimeToXmlGregorianCalendar( LocalDateTime localDateTime ) {
        if ( localDateTime == null ) {
            return null;
        }

        return datatypeFactory.newXMLGregorianCalendar(
            localDateTime.getYear(),
            localDateTime.getMonthValue(),
            localDateTime.getDayOfMonth(),
            localDateTime.getHour(),
            localDateTime.getMinute(),
            localDateTime.getSecond(),
            localDateTime.get( ChronoField.MILLI_OF_SECOND ),
            DatatypeConstants.FIELD_UNDEFINED );
    }
}
