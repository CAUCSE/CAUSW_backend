package net.causw.adapter.web;
import lombok.RequiredArgsConstructor;
import net.causw.application.notification.FirebasePushNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcm")
public class PushController {
    private final FirebasePushNotificationService fcmService;

    @PostMapping("/send")
    public ResponseEntity<String> sendTestNotification(
            @RequestParam("token") String token,
            @RequestParam("title") String title,
            @RequestParam("body") String body
    ) {
        try {
            fcmService.sendNotification(token, title, body);
            return ResponseEntity.ok("Notification sent successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send notification: " + e.getMessage());
        }
    }

}
