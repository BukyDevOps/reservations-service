package buky.example.reservationsservice.messaging.messages;

import buky.example.reservationsservice.enumerations.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationStatusChangedMessage implements Serializable{

    private Long userId;
    private Long receiverId;
    private Long reservationId;
    private ReservationStatus status;
}

