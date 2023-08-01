package buky.example.reservationsservice.controller;


import buky.example.reservationsservice.enumerations.ReservationStatus;
import buky.example.reservationsservice.model.Reservation;
import buky.example.reservationsservice.security.HasRole;
import buky.example.reservationsservice.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @HasRole("GUEST")
    public Reservation makeReservation(@RequestBody Reservation reservation, Long userId) {
        Reservation response;
        try {
            response = reservationService.makeReservation(reservation, userId);
        } catch (Exception e) {
            reservationService.dumpInvalid(reservation, userId);
            throw e;
        }
        return response;
    }

    /**Guest cancells*/
    @HasRole("GUEST")
    @DeleteMapping("/cancel/{id}")
    public Reservation cancelReservation(@PathVariable Long id, Long userId) {
        return reservationService.cancel(id, userId);
    }

    /**Host cancells*/
    @DeleteMapping("/withdraw/{id}")
    @HasRole("HOST")
    public Reservation withdrawReservation(@PathVariable Long id, Long userId) {
        return reservationService.withdraw(id, userId);
    }

    @PostMapping("/accept/{id}")
    @HasRole("HOST")
    public Reservation acceptReservation(@PathVariable Long id, Long userId) {
        return reservationService.acceptReservation(id, userId);
    }

    @PostMapping("/deny/{id}")
    @HasRole("HOST")
    public Reservation denyReservation(@PathVariable Long id, Long userId) {
        return reservationService.declineReservation(id, userId);
    }

    @GetMapping("/{id}")
    public Reservation getOne(@PathVariable Long id) {
        return reservationService.findOneReservation(id);
    }

    @GetMapping("/user-and-status")
    public List<Reservation> getAllByUserIdAndStatus(@RequestParam Long id, @RequestParam ReservationStatus reservationStatus) {
        return reservationService.findAllReservationsByUserIdAndStatus(id, reservationStatus);
    }

    @GetMapping("/accommodation-and-status")
    public List<Reservation> getAllByAccommodationIdAndStatus(@RequestParam Long id, @RequestParam ReservationStatus reservationStatus) {
        return reservationService.findAllReservationsByAccommodationIdAndStatus(id, reservationStatus);
    }

    @GetMapping("/user/{id}")
    public List<Reservation> getAllByUserId(@PathVariable Long id) {
        return reservationService.findAllReservationsByUserId(id);
    }

    @GetMapping("/accommodation/{id}")
    public List<Reservation> getAllByAccommodationId(@PathVariable Long id) {
        return reservationService.findAllReservationsByAccommodationId(id);
    }

    @GetMapping("/previous-reservations")
    public Boolean userHasPreviousReservations(@RequestParam Long userId, @RequestParam Long accommodationId) {
        return reservationService.isUserHasPreviousReservations(userId, accommodationId);
    }

    @GetMapping("/stayed-in")
    public Boolean userStayedIn(@RequestParam Long userId, @RequestParam Long accommodationId) {
        return reservationService.isUserStayedIn(userId, accommodationId);
    }

}
