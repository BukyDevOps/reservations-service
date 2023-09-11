package buky.example.reservationsservice.integration;

import buky.example.reservationsservice.enumerations.ReservationStatus;
import buky.example.reservationsservice.model.Reservation;
import buky.example.reservationsservice.repository.ReservationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test-containers")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReservationRepositoryTest {

    private final Long EXISTING_ID = 1L;
    private final Long INVALID_ID = 10L;

    @Autowired
    public ReservationRepository reservationRepository;


    @BeforeAll
    public void setUp() {
        Reservation r1 = Reservation.builder()
                .id(1L)
                .reservationStatus(ReservationStatus.ACCEPTED)
                .reservationStart(LocalDate.now().plusDays(2))
                .reservationEnd(LocalDate.now().plusDays(10))
                .priceByGuest(20.)
                .totalPrice(300.)
                .guestsNum(15)
                .hostId(2L)
                .userId(1L).build();
        Reservation r2 = Reservation.builder()
                .id(2L)
                .reservationStatus(ReservationStatus.ACCEPTED)
                .reservationStart(LocalDate.now().plusDays(20))
                .reservationEnd(LocalDate.now().plusDays(30))
                .priceByGuest(20.)
                .totalPrice(300.)
                .guestsNum(15)
                .hostId(2L)
                .userId(1L).build();
        Reservation r3 = Reservation.builder()
                .id(3L)
                .reservationStatus(ReservationStatus.DENIED)
                .reservationStart(LocalDate.now().plusDays(2))
                .reservationEnd(LocalDate.now().plusDays(10))
                .priceByGuest(20.)
                .totalPrice(300.)
                .guestsNum(15)
                .hostId(2L)
                .userId(1L).build();
        Reservation r4 = Reservation.builder()
                .id(4L)
                .reservationStatus(ReservationStatus.DENIED)
                .reservationStart(LocalDate.now().plusDays(2))
                .reservationEnd(LocalDate.now().plusDays(10))
                .priceByGuest(20.)
                .totalPrice(300.)
                .guestsNum(15)
                .hostId(2L)
                .userId(1L).build();

        this.reservationRepository.saveAll(List.of(r1,r2,r3,r4));
    }

    @Test
    public void findAllReservations() {

        List<Reservation> reservationList = reservationRepository.findAll();

        Assertions.assertEquals(4, reservationList.size());
    }

    @Test
    public void findReservation_whenExistsInDB_returnReservation(){
        Reservation reservation = reservationRepository.findById(EXISTING_ID).orElse(null);

        Assertions.assertNotNull(reservation);
        Assertions.assertEquals(EXISTING_ID, reservation.getId());
    }

    @Test
    public void findReservation_whenInvalidId_returnNull(){
        Reservation reservation = reservationRepository.findById(INVALID_ID).orElse(null);

        Assertions.assertNull(reservation);
    }
}
