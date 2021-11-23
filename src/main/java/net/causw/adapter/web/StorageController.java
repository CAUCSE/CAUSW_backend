package net.causw.adapter.web;

import net.causw.application.StorageService;
import net.causw.application.dto.UploadImageResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/storage")
public class StorageController {
    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/image/upload")
    public UploadImageResponseDto uploadImage(
            @RequestPart MultipartFile image, @RequestParam Optional<String> imageLocation
            ) {
        return this.storageService. uploadImageToGcs(image, imageLocation);
    }
}
