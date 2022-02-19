package net.causw.application;

import net.causw.application.dto.UploadImageResponseDto;
import net.causw.domain.model.ImageLocation;
import net.causw.infrastructure.GcpFileUploader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class StorageService {
    private final GcpFileUploader gcpFileUploader;

    public StorageService(GcpFileUploader gcpFileUploader) {
        this.gcpFileUploader = gcpFileUploader;
    }

    public UploadImageResponseDto uploadImageToGcs(MultipartFile image, Optional<String> imageLocation) {
        return UploadImageResponseDto.of(
                gcpFileUploader.uploadImageToGcp(image, ImageLocation.of(imageLocation.orElse("ETC")))
        );
    }
}
