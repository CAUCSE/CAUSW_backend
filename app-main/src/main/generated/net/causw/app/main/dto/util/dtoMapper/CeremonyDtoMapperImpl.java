package net.causw.app.main.dto.util.dtoMapper;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import net.causw.app.main.domain.model.entity.ceremony.Ceremony;
import net.causw.app.main.domain.model.entity.notification.CeremonyNotificationSetting;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.ceremony.CeremonyNotificationSettingResponseDto;
import net.causw.app.main.dto.ceremony.CeremonyNotificationSettingResponseDto.CeremonyNotificationSettingResponseDtoBuilder;
import net.causw.app.main.dto.ceremony.CeremonyResponseDto;
import net.causw.app.main.dto.ceremony.CeremonyResponseDto.CeremonyResponseDtoBuilder;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-21T16:18:45+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
@Component
public class CeremonyDtoMapperImpl implements CeremonyDtoMapper {

    @Override
    public CeremonyResponseDto toCeremonyResponseDto(Ceremony ceremony) {
        if ( ceremony == null ) {
            return null;
        }

        CeremonyResponseDtoBuilder ceremonyResponseDto = CeremonyResponseDto.builder();

        ceremonyResponseDto.id( ceremony.getId() );
        ceremonyResponseDto.description( ceremony.getDescription() );
        ceremonyResponseDto.startDate( ceremony.getStartDate() );
        ceremonyResponseDto.endDate( ceremony.getEndDate() );
        ceremonyResponseDto.category( ceremony.getCeremonyCategory() );
        ceremonyResponseDto.ceremonyState( ceremony.getCeremonyState() );
        ceremonyResponseDto.attachedImageUrlList( mapAttachedImages( ceremony.getCeremonyAttachImageList() ) );
        ceremonyResponseDto.note( ceremony.getNote() );
        ceremonyResponseDto.applicantStudentId( ceremonyUserStudentId( ceremony ) );
        ceremonyResponseDto.applicantName( ceremonyUserName( ceremony ) );

        return ceremonyResponseDto.build();
    }

    @Override
    public CeremonyNotificationSettingResponseDto toCeremonyNotificationSettingResponseDto(CeremonyNotificationSetting ceremonyNotificationSetting) {
        if ( ceremonyNotificationSetting == null ) {
            return null;
        }

        CeremonyNotificationSettingResponseDtoBuilder ceremonyNotificationSettingResponseDto = CeremonyNotificationSettingResponseDto.builder();

        ceremonyNotificationSettingResponseDto.isNotificationActive( ceremonyNotificationSetting.isNotificationActive() );
        ceremonyNotificationSettingResponseDto.isSetAll( ceremonyNotificationSetting.isSetAll() );
        Set<Integer> set = ceremonyNotificationSetting.getSubscribedAdmissionYears();
        if ( set != null ) {
            ceremonyNotificationSettingResponseDto.subscribedAdmissionYears( new HashSet<Integer>( set ) );
        }

        return ceremonyNotificationSettingResponseDto.build();
    }

    private String ceremonyUserStudentId(Ceremony ceremony) {
        if ( ceremony == null ) {
            return null;
        }
        User user = ceremony.getUser();
        if ( user == null ) {
            return null;
        }
        String studentId = user.getStudentId();
        if ( studentId == null ) {
            return null;
        }
        return studentId;
    }

    private String ceremonyUserName(Ceremony ceremony) {
        if ( ceremony == null ) {
            return null;
        }
        User user = ceremony.getUser();
        if ( user == null ) {
            return null;
        }
        String name = user.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
