package buky.example.reservationsservice.model;

import buky.example.reservationsservice.enumerations.ReservationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;
    private Long accommodationId;
    private int guestsNum;
    private double totalPrice;
    private double priceByGuest;
    private Date reservationStart;
    private Date reservationEnd;
    private ReservationStatus reservationStatus;
}
