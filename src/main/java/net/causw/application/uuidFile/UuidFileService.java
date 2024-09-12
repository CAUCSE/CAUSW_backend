package net.causw.application.uuidFile;

import com.amazonaws.services.s3.AmazonS3Client;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.adapter.persistence.repository.UuidFileRepository;
import net.causw.application.storage.StorageManager;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.FilePath;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public void deleteFile(UuidFile uuidFile) {
        super.deleteFile(uuidFile.getFileKey());
        uuidFileRepository.delete(uuidFile);
    }
}
