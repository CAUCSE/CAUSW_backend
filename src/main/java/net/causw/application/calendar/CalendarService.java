package net.causw.application.calendar;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.calendar.Calendar;
import net.causw.adapter.persistence.repository.calendar.CalendarRepository;
import net.causw.adapter.persistence.repository.uuidFile.CalendarAttachImageRepository;
import net.causw.adapter.persistence.uuidFile.joinEntity.CalendarAttachImage;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.calendar.CalendarCreateRequestDto;
import net.causw.application.dto.calendar.CalendarResponseDto;
import net.causw.application.dto.calendar.CalendarUpdateRequestDto;
import net.causw.application.dto.calendar.CalendarsResponseDto;
import net.causw.application.dto.util.dtoMapper.CalendarDtoMapper;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.uuidFile.FilePath;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@MeasureTime
@Service
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository calendarRepository;
    private final UuidFileService uuidFileService;
    private final CalendarAttachImageRepository calendarAttachImageRepository;

    @Transactional(readOnly = true)
    public CalendarsResponseDto findCalendarByYear(Integer year) {
        List<CalendarResponseDto> calendars = calendarRepository.findByYearOrderByMonthDesc(year).stream()
                .map(CalendarDtoMapper.INSTANCE::toCalendarResponseDto)
                .toList();

        return CalendarDtoMapper.INSTANCE.toCalendarsResponseDto(
                calendars.size(),
                calendars
        );
    }

    @Transactional(readOnly = true)
    public CalendarResponseDto findCalendar(String calendarId) {
        return CalendarDtoMapper.INSTANCE.toCalendarResponseDto(
                calendarRepository.findById(calendarId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CALENDAR_NOT_FOUND
                )
        ));
    }

    @Transactional(readOnly = true)
    public CalendarResponseDto findCalendar() {
        return calendarRepository.findFirstByOrderByYearDescMonthDesc()
                .map(CalendarDtoMapper.INSTANCE::toCalendarResponseDto)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CALENDAR_NOT_FOUND
                ));
    }

    @Transactional
    public CalendarResponseDto createCalendar(CalendarCreateRequestDto calendarCreateRequestDto, MultipartFile image) {
        UuidFile uuidFile = uuidFileService.saveFile(image, FilePath.CALENDAR);

        calendarRepository.findByYearAndMonth(calendarCreateRequestDto.getYear(), calendarCreateRequestDto.getMonth())
                .ifPresent(calendar -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.CALENDAR_ALREADY_EXIST
                    );
                });

        return CalendarDtoMapper.INSTANCE.toCalendarResponseDto(
                calendarRepository.save(
                        Calendar.of(
                                calendarCreateRequestDto.getYear(),
                                calendarCreateRequestDto.getMonth(),
                                uuidFile
                        )
                )
        );
    }

    @Transactional
    public CalendarResponseDto updateCalendar(String calendarId, CalendarUpdateRequestDto calendarUpdateRequestDto, MultipartFile image) {
        Calendar calendar = calendarRepository.findById(calendarId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CALENDAR_NOT_FOUND
                )
        );

        // 이미지가 없을 경우 기존 이미지를 사용하고, 이미지가 있을 경우 새로운 이미지로 교체 (Calendar의 이미지는 not null임)
        if ( !(image == null || image.isEmpty()) ) {
            calendar.getCalendarAttachImage().setUuidFile(
                    uuidFileService.updateFile(
                            calendar.getCalendarAttachImage().getUuidFile(),
                            image,
                            FilePath.CALENDAR
                    )
            );
        }

        CalendarAttachImage calendarAttachImage = calendar.getCalendarAttachImage();

        calendar.update(
                calendarUpdateRequestDto.getYear(),
                calendarUpdateRequestDto.getMonth(),
                calendarAttachImage
        );

        return CalendarDtoMapper.INSTANCE.toCalendarResponseDto(calendarRepository.save(calendar));
    }

    /**
     * @param calendarId 삭제할 캘린더 Id
     * @return 캘린더 단일 dto
     */
    @Transactional
    public CalendarResponseDto deleteCalendar(final String calendarId) {
        Calendar calendar = calendarRepository.findById(calendarId).orElseThrow(
            () -> new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                MessageUtil.CALENDAR_NOT_FOUND
            )
        );
        
        calendarAttachImageRepository.delete(calendar.getCalendarAttachImage());
        uuidFileService.deleteFile(calendar.getCalendarAttachImage().uuidFile);
        calendarRepository.delete(calendar);

        return CalendarDtoMapper.INSTANCE.toCalendarResponseDto(calendar);
    }
}
