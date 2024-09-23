package net.causw.adapter.web;

import lombok.RequiredArgsConstructor;
import net.causw.application.kafka.KafkaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaController {
    private final KafkaService kafkaService;

    /**
     * 메시지를 Kafka로 전송하는 REST 엔드포인트
     *
     * @param message 전송할 메시지
     * @return 전송 결과 메시지
     */
    @PostMapping("/send")
    public String sendMessageToKafka(@RequestParam("message") String message) {
        kafkaService.sendMessage(message);
        return "Message sent to Kafka: " + message;
    }
}
