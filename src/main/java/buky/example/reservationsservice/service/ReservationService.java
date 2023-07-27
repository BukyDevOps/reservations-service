package buky.example.reservationsservice.service;

import buky.example.reservationsservice.enumerations.ReservationStatus;
import buky.example.reservationsservice.exceptions.NotFoundException;
import buky.example.reservationsservice.model.Reservation;
import buky.example.reservationsservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;


    public Reservation makeReservation(Reservation reservation) {
        //TODO checks [accommodationId valid & usercount valid & date range valid] + set user from jwt
        //TODO check if autoApproved and set status...
        //TODO ODREDITI TOTAL PRICE + PRICE BY GUEST
        return reservationRepository.save(reservation);
    }

    //guest cancells
    public void cancel(Long id) {
        var reservation = reservationRepository.findById(id).orElseThrow(NotFoundException::new);
        //TODO check if deletion permitted [if more than ] day left etc...
        reservation.setReservationStatus(ReservationStatus.CANCELED) ;
        reservationRepository.save(reservation);
    }

    //host cancells
    public void withdraw(Long id) {
        var reservation = reservationRepository.findById(id).orElseThrow(NotFoundException::new);
        reservation.setReservationStatus(ReservationStatus.WITHDRAWN) ;
        reservationRepository.save(reservation);
    }

    public Reservation acceptReservation(Long id) {
        var reservation = reservationRepository.findById(id).orElseThrow(NotFoundException::new);
        reservation.setReservationStatus(ReservationStatus.ACCEPTED);
        return reservationRepository.save(reservation);
    }

    public Reservation denyReservation(Long id) {
        var reservation = reservationRepository.findById(id).orElseThrow(NotFoundException::new);
        reservation.setReservationStatus(ReservationStatus.DENIED);
        return reservationRepository.save(reservation);
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation findOneReservation(Long id) {
        return reservationRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    public List<Reservation> findAllReservationsByUserId(Long id) {
        return reservationRepository.findByUserId(id);
    }

    public List<Reservation> findAllReservationsByAccommodationId(Long id) {
        return reservationRepository.findByAccommodationId(id);
    }

    public List<Reservation> findAllReservationsByUserIdAndStatus(Long id, ReservationStatus reservationStatus) {
        return reservationRepository.findByUserIdAndReservationStatus(id, reservationStatus);
    }

    public List<Reservation> findAllReservationsByAccommodationIdAndStatus(Long id,  ReservationStatus reservationStatus) {
        return reservationRepository.findByAccommodationIdAndReservationStatus(id, reservationStatus);
    }


}
