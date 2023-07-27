package buky.example.reservationsservice.controller;


import buky.example.reservationsservice.enumerations.ReservationStatus;
import buky.example.reservationsservice.model.Reservation;
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
    public Reservation makeReservation(@RequestBody Reservation reservation) {
        //TODO set userid from jwt... + ODREDITI TOTAL PRICE
        return reservationService.makeReservation(reservation);
    }

    /**Guest cancells*/
    @DeleteMapping("/cancel/{id}")
    public Boolean cancelReservation(@PathVariable Long id) {
        //TODO set userid from jwt...
        reservationService.cancel(id);
        return true;
    }

    /**Host cancells*/
    @DeleteMapping("/withdraw/{id}")
    public Boolean withdrawReservation(@PathVariable Long id) {
        //TODO set userid from jwt...
        reservationService.withdraw(id);
        return true;
    }

    @PostMapping("/accept/{id}")
    public Reservation acceptReservation(@PathVariable Long id) {
        //TODO set userid from jwt...
        return reservationService.acceptReservation(id);
    }

    @PostMapping("/deny/{id}")
    public Reservation denyReservation(@PathVariable Long id) {
        //TODO set userid from jwt...
        return reservationService.denyReservation(id);
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

}
