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

import java.time.LocalDate;

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
    private Integer guestsNum;
    private Double totalPrice;
    private Double priceByGuest;
    private LocalDate reservationStart;
    private LocalDate reservationEnd;
    private ReservationStatus reservationStatus;
}
