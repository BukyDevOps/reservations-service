package buky.example.reservationsservice.messaging.consumers;


import buky.example.reservationsservice.messaging.messages.UserDeletionRequestMessage;
import buky.example.reservationsservice.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ReservationService reservationService;

    @KafkaListener(topics = "user-deletion-request-topic", containerFactory = "userDeletionRequest")
    public void userDeletionRequest(UserDeletionRequestMessage message) {
        reservationService.userDeletionRequest(message);
    }

}
