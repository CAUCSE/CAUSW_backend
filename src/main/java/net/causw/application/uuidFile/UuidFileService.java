package net.causw.application.uuidFile;

import com.amazonaws.services.s3.AmazonS3Client;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.adapter.persistence.repository.UuidFileRepository;
import net.causw.application.storage.StorageManager;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.FileExtensionType;
import net.causw.domain.model.enums.FilePath;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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

    public UuidFile findUuidFileById(@NotBlank String id) {
        return uuidFileRepository.findById(id).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.FILE_NOT_FOUND));
    }

    public UuidFile findUuidFileByFileUrl(@NotBlank String fileUrl) {
        return uuidFileRepository.findByFileUrl(fileUrl).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.FILE_NOT_FOUND));
    }

    @Transactional
    public UuidFile saveFile(@NotNull MultipartFile file, @NotNull FilePath filePath) {
        this.validateFile(file, filePath);

        String uuid = UUID.randomUUID().toString();

        String originFileName = file.getOriginalFilename();

        String rawFileName = this.getRawFileName(originFileName);
        String extension = this.getExtension(originFileName);


        Map<StorageManager.FileInfo, String> fileInfoStringMap = super.uploadFile(file, rawFileName, extension, filePath, uuid);

        UuidFile uuidFile = UuidFile.of(
                uuid,
                fileInfoStringMap.get(FileInfo.FILE_KEY),
                fileInfoStringMap.get(FileInfo.FILE_URL),
                rawFileName,
                extension,
                filePath
        );

        return uuidFileRepository.save(uuidFile);
    }

    @Transactional
    public List<UuidFile> saveFileList(@NotNull List<MultipartFile> fileList, @NotNull FilePath filePath) {
        this.validateFileListSize(fileList, filePath);
        return fileList.stream()
                .map(file -> this.saveFile(file, filePath))
                .toList();
    }

    @Transactional
    public UuidFile updateFile(UuidFile priorUuidFile, @NotNull MultipartFile file, @NotNull FilePath filePath) {
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
    public void deleteFile(@NotNull UuidFile uuidFile) {
        if (uuidFile == null) {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FILE_NOT_FOUND);
        }

        super.deleteFile(uuidFile.getFileKey());
        uuidFileRepository.delete(uuidFile);
    }

    @Transactional
    public void deleteFileList(@NotNull List<UuidFile> uuidFileList) {
        for (UuidFile uuidFile : uuidFileList) {
            this.deleteFile(uuidFile);
        }
    }

    // Private Methods
    private String getRawFileName(String originFileName) {
        if (originFileName == null) {
            throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.FILE_NAME_IS_NULL);
        }
        return StringUtils.stripFilenameExtension(originFileName);
    }

    private String getExtension(String originFileName) {
        String extension = StringUtils.getFilenameExtension(originFileName);
        if (extension == null) {
            throw new BadRequestException(ErrorCode.INVALID_FILE_EXTENSION, MessageUtil.FILE_EXTENSION_IS_NULL);
        }
        return extension;
    }

    // 파일 크기, 확장자, Null 검증
    private void validateFile(MultipartFile file, FilePath filePath) {
        if (file == null) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_IS_NULL);
        }

        String extension = this.getExtension(file.getOriginalFilename()).toUpperCase();

        if (file.getSize() > filePath.getMaxFileSize()) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.FILE_SIZE_EXCEEDED);
        }

        boolean isValidExtension = false;
        for (FileExtensionType fileExtensionType : filePath.getFileExtensionList()) {
            for (String extensionType : fileExtensionType.getExtensionList()) {
                if (extension.equals(extensionType)) {
                    isValidExtension = true;
                    break;
                }
            }
        }
        if (!isValidExtension) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.INVALID_FILE_EXTENSION);
        }
    }

    // 파일 개수 검증
    private void validateFileListSize(List<MultipartFile> fileList, FilePath filePath) {
        if (fileList.isEmpty()) {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FILE_IS_NULL);
        }

        if (fileList.size() > filePath.getMaxFileCount()) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.NUMBER_OF_FILES_EXCEEDED);
        }
    }

}
