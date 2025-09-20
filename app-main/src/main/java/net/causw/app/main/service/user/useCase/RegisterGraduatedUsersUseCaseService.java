package net.causw.app.main.service.user.useCase;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.dto.user.BatchRegisterResponseDto;
import net.causw.app.main.dto.user.GraduatedUserRegisterRequestDto;
import net.causw.app.main.service.user.UserService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RegisterGraduatedUsersUseCaseService {

	private static final int PRIVACY_POLICY_COLUMN = 2;
	private static final int NAME_COLUMN = 3;
	private static final int ADMISSION_YEAR_COLUMN = 4;
	private static final int GRADUATION_YEAR_COLUMN = 5;
	private static final int EMAIL_COLUMN = 6;
	private static final int PHONE_NUMBER_COLUMN = 7;
	private static final int MAJOR_COLUMN = 8;
	private static final int STUDENT_ID_COLUMN = 9;

	private final UserService userService;

	public BatchRegisterResponseDto execute(MultipartFile csvFile) {
		validateCsvFileSize(csvFile);

		int success = 0;
		int fail = 0;
		List<String> failureMessages = new ArrayList<>();

		List<CSVRecord> records = parse(csvFile);
		validateCsvRecordDuplicates(records);

		for (CSVRecord record : records) {
			try {
				GraduatedUserRegisterRequestDto dto = toDto(record);
				userService.registerGraduatedUser(dto);

				success++;

			} catch (Exception e) {
				String message = "Row " + (record.getRecordNumber() - 1) + ": " + e.getMessage();
				failureMessages.add(message);
				log.warn("CSV 파일로 졸업생 등록 실패 - {}", message, e);

				fail++;
			}
		}

		return new BatchRegisterResponseDto(success, fail, failureMessages);
	}

	private static void validateCsvFileSize(MultipartFile csvFile) {
		if (csvFile.getSize() > StaticValue.CSV_FILE_SIZE) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				"CSV " + MessageUtil.FILE_SIZE_EXCEEDED
			);
		}
	}

	/**
	 * 이메일, 전화번호, 학번 컬럼을 기준으로 사용자 정보 중복을 감지합니다.
	 *
	 * @param records
	 */
	private void validateCsvRecordDuplicates(List<CSVRecord> records) {
		Set<String> emailSet = new HashSet<>();
		Set<String> phoneNumberSet = new HashSet<>();
		Set<String> studentIdSet = new HashSet<>();

		for (CSVRecord record : records) {
			String rowLabel = "Row " + (record.getRecordNumber() - 1) + ": ";

			String email = record.get(EMAIL_COLUMN).trim();
			if (!email.isEmpty() && !emailSet.add(email)) {
				throw new BadRequestException(
					ErrorCode.INVALID_PARAMETER,
					rowLabel + MessageUtil.EMAIL_ALREADY_EXIST
				);
			}

			String phoneNumber = record.get(PHONE_NUMBER_COLUMN).trim();
			if (!phoneNumber.isEmpty() && !phoneNumberSet.add(phoneNumber)) {
				throw new BadRequestException(
					ErrorCode.INVALID_PARAMETER,
					rowLabel + MessageUtil.PHONE_NUMBER_ALREADY_EXIST
				);
			}

			String studentId = record.get(STUDENT_ID_COLUMN).trim();
			if (!studentId.isEmpty() && !studentIdSet.add(studentId)) {
				throw new BadRequestException(
					ErrorCode.INVALID_PARAMETER,
					rowLabel + MessageUtil.STUDENT_ID_ALREADY_EXIST
				);
			}
		}
	}

	private List<CSVRecord> parse(MultipartFile csvFile) {
		try (Reader reader = new InputStreamReader(csvFile.getInputStream());
			 CSVParser csvParser = CSVParser.parse(reader, CSVFormat.DEFAULT)
		) {
			return StreamSupport.stream(csvParser.spliterator(), false)
				.skip(1) // 헤더 제외
				.toList();

		} catch (IOException e) {
			throw new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				"CSV " + MessageUtil.FILE_READ_FAIL
			);
		}
	}

	private GraduatedUserRegisterRequestDto toDto(CSVRecord record) {
		boolean isPrivacyPolicyAccepted = record.get(PRIVACY_POLICY_COLUMN).trim().equals("동의");
		if (!isPrivacyPolicyAccepted) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.PRIVACY_POLICY_REQUIRED
			);
		}

		return new GraduatedUserRegisterRequestDto(
			record.get(NAME_COLUMN).trim(),
			"user-" + UUID.randomUUID().toString().substring(0, 8), // 임시 닉네임
			Integer.parseInt(record.get(ADMISSION_YEAR_COLUMN).trim()),
			Integer.parseInt(record.get(GRADUATION_YEAR_COLUMN).trim()),
			record.get(EMAIL_COLUMN).trim(),
			record.get(EMAIL_COLUMN).trim(), // 임시 비밀번호
			record.get(PHONE_NUMBER_COLUMN).trim(),
			record.get(MAJOR_COLUMN).trim(),
			record.get(STUDENT_ID_COLUMN).trim().isEmpty() ? null : record.get(9).trim()
		);
	}
}
