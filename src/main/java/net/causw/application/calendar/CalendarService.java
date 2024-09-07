package net.causw.application.calendar;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.calendar.Calendar;
import net.causw.adapter.persistence.repository.CalendarRepository;
import net.causw.application.dto.calendar.CalendarCreateRequestDto;
import net.causw.application.dto.calendar.CalendarResponseDto;
import net.causw.application.dto.calendar.CalendarUpdateRequestDto;
import net.causw.application.dto.calendar.CalendarsResponseDto;
import net.causw.application.dto.util.DtoMapper;
import net.causw.application.storage.StorageService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository calendarRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public CalendarsResponseDto findCalendarByYear(Integer year) {
        List<CalendarResponseDto> calendars = calendarRepository.findByYearOrderByMonthAsc(year).stream()
                .map(DtoMapper.INSTANCE::toCalendarResponseDto)
                .toList();

        return DtoMapper.INSTANCE.toCalendarsResponseDto(
                calendars.size(),
                calendars
        );
    }

    @Transactional(readOnly = true)
    public CalendarResponseDto findCalendar(String calendarId) {
        return DtoMapper.INSTANCE.toCalendarResponseDto(
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
                .map(DtoMapper.INSTANCE::toCalendarResponseDto)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CALENDAR_NOT_FOUND
                ));
    }

    @Transactional
    public CalendarResponseDto createCalendar(CalendarCreateRequestDto calendarCreateRequestDto) {
        calendarRepository.findByYearAndMonth(calendarCreateRequestDto.getYear(), calendarCreateRequestDto.getMonth())
                .ifPresent(calendar -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.CALENDAR_ALREADY_EXIST
                    );
                });

        return DtoMapper.INSTANCE.toCalendarResponseDto(
                calendarRepository.save(
                        Calendar.of(
                                calendarCreateRequestDto.getYear(),
                                calendarCreateRequestDto.getMonth(),
                                storageService.uploadFile(calendarCreateRequestDto.getImage(), "CALENDAR")
                        )
                )
        );
    }

    @Transactional
    public CalendarResponseDto updateCalendar(String calendarId, CalendarUpdateRequestDto calendarUpdateRequestDto) {
        Calendar calendar = calendarRepository.findById(calendarId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CALENDAR_NOT_FOUND
                )
        );

        calendar.update(
                calendarUpdateRequestDto.getYear(),
                calendarUpdateRequestDto.getMonth(),
                storageService.uploadFile(calendarUpdateRequestDto.getImage(), "CALENDAR")
        );

        return DtoMapper.INSTANCE.toCalendarResponseDto(calendarRepository.save(calendar));
    }
}
