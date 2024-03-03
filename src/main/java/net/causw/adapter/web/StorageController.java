package net.causw.adapter.web;

import lombok.RequiredArgsConstructor;
import net.causw.application.storage.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/storage", produces = APPLICATION_JSON_VALUE)
public class StorageController {

    private final StorageService storageService;

    @PostMapping("")
    public ResponseEntity<String> post(
        @RequestPart("file") MultipartFile multipartFile) {
        return ResponseEntity.ok(storageService.uploadFile(multipartFile));
    }
}