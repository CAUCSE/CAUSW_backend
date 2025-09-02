package net.causw.app.main.service.user.useCase;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.dto.user.BatchRegisterResponseDto;
import net.causw.app.main.dto.user.GraduatedUserRegisterRequestDto;
import net.causw.app.main.service.user.UserService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterGraduatedUsersUseCaseService {

    private final UserService userService;


    public BatchRegisterResponseDto execute(MultipartFile csvFile) {
        if (csvFile.getSize() > StaticValue.CSV_FILE_SIZE) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "CSV " + MessageUtil.FILE_SIZE_EXCEEDED
            );
        }

        int success = 0;
        int fail = 0;
        List<String> failureMessages = new ArrayList<>();

        List<CSVRecord> records = parse(csvFile);

        for (CSVRecord record : records) {
            try {
                GraduatedUserRegisterRequestDto dto = toDto(record);
                userService.registerGraduatedUser(dto);
                success++;

            } catch (Exception e) {
                fail++;
                failureMessages.add("Row " + (record.getRecordNumber() - 1) + ": " + e.getMessage());
            }
        }

        return new BatchRegisterResponseDto(success, fail, failureMessages);
    }

    private List<CSVRecord> parse(MultipartFile csvFile) {
        try (Reader reader = new InputStreamReader(csvFile.getInputStream());
             CSVParser csvParser = CSVParser.parse(reader, CSVFormat.DEFAULT)
        ) {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy/MM/dd h:mm:ss a", Locale.KOREAN);

            return StreamSupport.stream(csvParser.spliterator(), false)
                    .skip(1) // 헤더 무시
                    .sorted((r1, r2) -> {
                        LocalDateTime t1 = LocalDateTime.parse(r1.get(0).split("GMT")[0].trim(), formatter);
                        LocalDateTime t2 = LocalDateTime.parse(r2.get(0).split("GMT")[0].trim(), formatter);
                        return t2.compareTo(t1); // 최신 등록순 정렬
                    }).toList();

        } catch (IOException e) {
            throw new InternalServerException(
                    ErrorCode.INTERNAL_SERVER,
                    "CSV " + MessageUtil.FILE_READ_FAIL
            );
        }
    }

    private GraduatedUserRegisterRequestDto toDto(CSVRecord record) {
        boolean isPrivacyPolicyAccepted = record.get(2).trim().equals("동의");
        if (!isPrivacyPolicyAccepted) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    MessageUtil.PRIVACY_POLICY_REQUIRED
            );
        }

        return GraduatedUserRegisterRequestDto.builder()
                .name(record.get(3).trim())
                .nickname("user-" + UUID.randomUUID().toString().substring(0, 8)) // 임시 닉네임
                .admissionYear(Integer.parseInt(record.get(4).trim()))
                .graduationYear(Integer.parseInt(record.get(5).trim()))
                .email(record.get(6).trim())
                .password(record.get(6).trim()) // 임시 비밀번호
                .phoneNumber(record.get(7).trim())
                .major(record.get(8).trim())
                .studentId(record.get(9).trim().isEmpty() ? null : record.get(9).trim())
                .build();
    }
}
