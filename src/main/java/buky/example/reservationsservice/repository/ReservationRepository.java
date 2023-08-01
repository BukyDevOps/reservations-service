package buky.example.reservationsservice.repository;

import buky.example.reservationsservice.enumerations.ReservationStatus;
import buky.example.reservationsservice.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByAccommodationId(@NonNull Long id);

    List<Reservation> findByUserId(@NonNull Long id);

    List<Reservation> findByAccommodationIdAndReservationStatus(@NonNull Long id, @NonNull ReservationStatus status);

    List<Reservation> findByUserIdAndReservationStatus(@NonNull Long id, @NonNull ReservationStatus status);

    List<Reservation> findAllByReservationStatus(@NonNull ReservationStatus reservationStatus);

    @Query("""
            select (count(r) > 0) from Reservation r
            where
            r.accommodationId = ?1 and 
            r.reservationStatus = ?4 and 
            r.reservationStart between ?2 and ?3 and 
            r.reservationEnd between ?2 and ?3""")
    boolean existsByAccommodationIdAndReservationOverlap(@NonNull Long accommodationId, @NonNull LocalDate reservationStart, @NonNull LocalDate reservationEnd, @NonNull ReservationStatus status);

    @Query("""
            select r from Reservation r
            where 
            r.accommodationId = ?1 and
            r.reservationStatus = ?4 and 
            r.reservationStart between ?2 and ?3 and 
            r.reservationEnd between ?2 and ?3""")
    List<Reservation> findByAccommodationIdAndReservationOverlap(@NonNull Long accommodationId, @NonNull LocalDate reservationStart, @NonNull LocalDate reservationEnd, @NonNull ReservationStatus status);

    @Transactional
    @Modifying
    @Query("""
            update Reservation r set r.reservationStatus = ?1
            where r.accommodationId = ?2 and r.reservationStatus = ?3 and r.reservationStart between ?4 and ?5 and r.reservationEnd between ?4 and ?5""")
    int updateReservationStatusByDateOverlap(ReservationStatus newStatus, Long accommodationId, ReservationStatus reservationStatus, LocalDate reservationStart, LocalDate reservationEnd);

    boolean existsByUserIdAndAccommodationIdAndReservationStatusNot(Long userId, Long accommodationId, ReservationStatus reservationStatus);

    boolean existsByUserIdAndAccommodationIdAndReservationEndBeforeAndReservationStatusIn(Long userId, Long accommodationId, LocalDate reservationEnd, Collection<ReservationStatus> reservationStatuses);



}
