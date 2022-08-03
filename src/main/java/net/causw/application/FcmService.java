package net.causw.application;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import net.causw.adapter.persistence.User;
import net.causw.application.spi.*;
import net.causw.domain.model.*;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FcmService {
    private final BoardPort boardPort;
    private final UserPort userPort;
    private final CirclePort circlePort;
    private final CircleMemberPort circleMemberPort;
    private final DeviceTokenPort deviceTokenPort;
    private final FavoriteBoardPort favoriteBoardPort;
    private final Validator validator;

    private final String FCM_SEND_API_URL = "https://fcm.googleapis.com/v1/projects/causw-dev/messages:send";
    private final ObjectMapper objectMapper;

    public FcmService(
            BoardPort boardPort,
            UserPort userPort,
            CirclePort circlePort,
            CircleMemberPort circleMemberPort,
            DeviceTokenPort deviceTokenPort,
            FavoriteBoardPort favoriteBoardPort,
            Validator validator,
            ObjectMapper objectMapper
    ) {
        this.boardPort = boardPort;
        this.userPort = userPort;
        this.circlePort = circlePort;
        this.circleMemberPort = circleMemberPort;
        this.deviceTokenPort = deviceTokenPort;
        this.favoriteBoardPort = favoriteBoardPort;
        this.validator = validator;
        this.objectMapper = objectMapper;
    }

    public void sendMessageToSpecific(List<DeviceTokenDomainModel> targetDevices, String title, String body, @Nullable String image) {
        List<String> targetUserIds = targetDevices.stream()
                        .map(device -> device.getDeviceToken())
                                .collect(Collectors.toList());

        targetUserIds.forEach(
                id -> {
                    try {
                        this.sendMessageTo(id, title, body, image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public void broadcastMessageTo(CircleDomainModel circle, String title, String body, @Nullable String image) {
        List<DeviceTokenDomainModel> devices = this.circleMemberPort.findByCircleId(
                circle.getId(), CircleMemberStatus.MEMBER).stream()
                .map(circleMember -> circleMember.getUserId())
                .collect(Collectors.toList())
                .stream()
                .map(id -> this.deviceTokenPort.findByUserId(id))
                .collect(ArrayList::new, List::addAll, List::addAll);

        sendMessageToSpecific(devices, title, body, image);
    }

    public void broadcastMessageTo(BoardDomainModel board, String title, String body, @Nullable String image) {
        List<DeviceTokenDomainModel> devices = this.favoriteBoardPort.findByBoardId(board.getId())
                .stream()
                .map(favoriteBoard -> favoriteBoard.getUserDomainModel().getId())
                .collect(Collectors.toList())
                .stream()
                .map(id -> this.deviceTokenPort.findByUserId(id))
                .collect(ArrayList::new, List::addAll, List::addAll);

        sendMessageToSpecific(devices, title, body, image);
    }

    private void sendMessageTo(String targetToken, String title, String body, String image) throws IOException {
        String message = makeMessage(targetToken, title, body, image);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(FCM_SEND_API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.body().string());
    }

    private String makeMessage(String targetToken, String title, String body, String image) throws JsonParseException, JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(image)
                                .build()
                        ).build()).validateOnly(false).build();

        return objectMapper.writeValueAsString(fcmMessage);
    }

    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "causw-dev-firebase-adminsdk.json";

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
