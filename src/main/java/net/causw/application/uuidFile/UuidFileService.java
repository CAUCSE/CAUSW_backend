package net.causw.application.uuidFile;

import com.amazonaws.services.s3.AmazonS3Client;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.adapter.persistence.repository.UuidFileRepository;
import net.causw.application.storage.StorageManager;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.FilePath;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UuidFileService extends StorageManager {

    private final UuidFileRepository uuidFileRepository;

    public UuidFileService(AmazonS3Client amazonS3Client, UuidFileRepository uuidFileRepository) {
        super(amazonS3Client);
        this.uuidFileRepository = uuidFileRepository;
    }

    public UuidFile findUuidFileById(String id) {
        return uuidFileRepository.findById(id).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.FILE_NOT_FOUND));
    }

    public UuidFile findUuidFileByFileUrl(String fileUrl) {
        return uuidFileRepository.findByFileUrl(fileUrl).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.FILE_NOT_FOUND));
    }

    @Transactional
    public UuidFile saveFile(MultipartFile file, FilePath filePath) {
        this.validateFileIsNull(file);
        this.validateFileSize(file, filePath);

        String uuid = UUID.randomUUID().toString();

        Map<StorageManager.FileInfo, String> fileInfoStringMap = super.uploadFile(file, filePath, uuid);

        UuidFile uuidFile = UuidFile.of(
                uuid,
                fileInfoStringMap.get(FileInfo.FILE_KEY),
                fileInfoStringMap.get(FileInfo.FILE_URL),
                filePath
        );

        return uuidFileRepository.save(uuidFile);
    }

    @Transactional
    public List<UuidFile> saveFileList(List<MultipartFile> fileList, FilePath filePath) {
        this.validateFileListIsEmpty(fileList);
        this.validateFileListSize(fileList, filePath);
        return fileList.stream()
                .map(file -> this.saveFile(file, filePath))
                .toList();
    }

    @Transactional
    public UuidFile updateFile(UuidFile priorUuidFile, MultipartFile file, FilePath filePath) {
        if (priorUuidFile != null) {
            this.deleteFile(priorUuidFile);
        }
        return this.saveFile(file, filePath);
    }

    @Transactional
    public List<UuidFile> updateFileList(List<UuidFile> priorUuidFileList, List<MultipartFile> fileList, FilePath filePath) {
        if (!priorUuidFileList.isEmpty()) {
            this.deleteFileList(priorUuidFileList);
        }

        return this.saveFileList(fileList, filePath);
    }

    @Transactional
    public void deleteFile(UuidFile uuidFile) {
        if (uuidFile == null) {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FILE_NOT_FOUND);
        }

        super.deleteFile(uuidFile.getFileKey());
        uuidFileRepository.delete(uuidFile);
    }

    @Transactional
    public void deleteFileList(List<UuidFile> uuidFileList) {
        for (UuidFile uuidFile : uuidFileList) {
            this.deleteFile(uuidFile);
        }
    }

    private void validateFileIsNull(MultipartFile file) {
        if (file == null) {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FILE_IS_NULL);
        }
    }

    private void validateFileListIsEmpty(List<MultipartFile> fileList) {
        if (fileList.isEmpty()) {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FILE_IS_NULL);
        }
    }

    // 파일 크기 검증
    private void validateFileSize(MultipartFile file, FilePath filePath) {
        if (filePath.equals(FilePath.USER_PROFILE)) {
            if (file.getSize() > StaticValue.USER_PROFILE_IMAGE_SIZE) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_SIZE_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.USER_ADMISSION)) {
            if (file.getSize() > StaticValue.USER_ADMISSION_IMAGE_SIZE) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_SIZE_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.USER_ACADEMIC_RECORD_APPLICATION)) {
            if (file.getSize() > StaticValue.USER_ACADEMIC_RECORD_APPLICATION_IMAGE_SIZE) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_SIZE_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.CIRCLE_PROFILE)) {
            if (file.getSize() > StaticValue.CIRCLE_PROFILE_IMAGE_SIZE) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_SIZE_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.POST)) {
            if (file.getSize() > StaticValue.POST_IMAGE_SIZE) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_SIZE_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.CALENDAR)) {
            if (file.getSize() > StaticValue.CALENDAR_IMAGE_SIZE) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_SIZE_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.EVENT)) {
            if (file.getSize() > StaticValue.EVENT_IMAGE_SIZE) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_SIZE_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.ETC)) {
            if (file.getSize() > StaticValue.ETC_FILE_SIZE) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_SIZE_EXCEEDED);
            }
        } else {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.INVALID_FILE_PATH);
        }
    }

    // 파일 개수 검증
    private void validateFileListSize(List<MultipartFile> fileList, FilePath filePath) {
        if (filePath.equals(FilePath.USER_PROFILE)) {
            if (fileList.size() > StaticValue.MAX_NUM_USER_PROFILE_IMAGE) {
                throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.NUMBER_OF_FILES_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.USER_ADMISSION)) {
            if (fileList.size() > StaticValue.MAX_NUM_USER_ADMISSION_IMAGE) {
                throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.NUMBER_OF_FILES_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.USER_ACADEMIC_RECORD_APPLICATION)) {
            if (fileList.size() > StaticValue.MAX_NUM_USER_ACADEMIC_RECORD_APPLICATION_IMAGE) {
                throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.NUMBER_OF_FILES_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.CIRCLE_PROFILE)) {
            if (fileList.size() > StaticValue.MAX_NUM_CIRCLE_PROFILE_IMAGE) {
                throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.NUMBER_OF_FILES_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.POST)) {
            if (fileList.size() > StaticValue.MAX_NUM_POST_IMAGE) {
                throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.NUMBER_OF_FILES_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.CALENDAR)) {
            if (fileList.size() > StaticValue.MAX_NUM_CALENDAR_IMAGE) {
                throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.NUMBER_OF_FILES_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.EVENT)) {
            if (fileList.size() > StaticValue.MAX_NUM_EVENT_IMAGE) {
                throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.NUMBER_OF_FILES_EXCEEDED);
            }
        } else if (filePath.equals(FilePath.ETC)) {
            if (fileList.size() > StaticValue.MAX_NUM_ETC_FILE) {
                throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.NUMBER_OF_FILES_EXCEEDED);
            }
        } else {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.INVALID_FILE_PATH);
        }
    }

}
