package net.causw.adapter.web;
import lombok.RequiredArgsConstructor;
import net.causw.application.notification.FirebasePushNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcm")
public class PushController {
    private final FirebasePushNotificationService fcmService;

    @PostMapping("/send")
    public ResponseEntity<String> sendTestNotification(
            @RequestParam String token,
            @RequestParam String title,
            @RequestParam String body
    ) {
        fcmService.sendNotification(token, title, body);
        return ResponseEntity.ok("Notification sent successfully!");
    }
}
