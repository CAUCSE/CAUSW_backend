package net.causw.application.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaService {

    private static final String TOPIC = "test";  // Kafka 토픽 이름
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Kafka로 메시지를 전송하는 메서드
     *
     * @param message 전송할 메시지
     */
    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
        System.out.println("Produced message: " + message);
    }

    /**
     * Kafka에서 메시지를 수신하는 메서드
     *
     * @param message 수신한 메시지
     */
    @KafkaListener(topics = TOPIC, groupId = "test-group")
    public void consumeMessage(String message) {
        System.out.println("Consumed message: " + message);
    }
}