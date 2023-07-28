package buky.example.reservationsservice.messaging.producers;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccommodationQueryProducer {

    private final KafkaTemplate<String, Long> kafkaTemplate;
    private static final String TOPIC = "accommodations_topic";

    public void sendAccommodationRequest(Long id) {
        kafkaTemplate.send(TOPIC, id);
    }
}
