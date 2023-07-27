package buky.example.reservationsservice.repository;

import buky.example.reservationsservice.enumerations.ReservationStatus;
import buky.example.reservationsservice.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation>findByAccommodationId(Long id);
    List<Reservation>findByUserId(Long id);
    List<Reservation>findByAccommodationIdAndReservationStatus(Long id, ReservationStatus status);
    List<Reservation>findByUserIdAndReservationStatus(Long id, ReservationStatus status);
    List<Reservation>findAllByReservationStatus(ReservationStatus reservationStatus);
}
