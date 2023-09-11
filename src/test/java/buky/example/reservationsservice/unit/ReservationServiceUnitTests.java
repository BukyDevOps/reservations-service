package buky.example.reservationsservice.unit;

import buky.example.reservationsservice.dto.*;
import buky.example.reservationsservice.enumerations.ReservationStatus;
import buky.example.reservationsservice.exceptions.DateNotAvailableException;
import buky.example.reservationsservice.messaging.producers.KafkaProducer;
import buky.example.reservationsservice.model.Reservation;
import buky.example.reservationsservice.repository.ReservationRepository;
import buky.example.reservationsservice.service.ReservationService;
import buky.example.reservationsservice.util.RestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@ActiveProfiles("test-containers")
@SpringBootTest
public class ReservationServiceUnitTests {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestUtil restUtil;

    @Mock
    private KafkaProducer publisher;

    private static Reservation mockReservation;
    private static Reservation invalidMockReservation;
    private static AccommodationDto mockAccommodationDto;

    @BeforeAll
    public static void setUp(){
        mockReservation = Reservation.builder()
                .id(1L)
                .userId(1L)
                .accommodationId(1L)
                .hostId(2L)
                .guestsNum(5)
                .totalPrice(800.)
                .priceByGuest(160.)
                .reservationStart(LocalDate.now().plusDays(2))
                .reservationEnd(LocalDate.now().plusDays(10))
                .reservationStatus(ReservationStatus.PENDING)
                .build();

        invalidMockReservation = Reservation.builder()
                .id(1L)
                .userId(1L)
                .accommodationId(1L)
                .hostId(2L)
                .guestsNum(5)
                .totalPrice(800.)
                .priceByGuest(160.)
                .reservationStart(LocalDate.now().plusDays(15))
                .reservationEnd(LocalDate.now().plusDays(10))
                .reservationStatus(ReservationStatus.PENDING)
                .build();

        AccommodationAvailabilityDto accommodationAvailabilityDto = AccommodationAvailabilityDto.builder()
                .id(1L)
                .allRangePeriods(List.of(RangePeriodDto.builder().id(1L)
                        .startDate(LocalDate.now().minusDays(20))
                        .endDate(LocalDate.now().plusDays(20)).build()))
                .allPatternPeriods(new ArrayList<>())
                .price(PriceDto.builder()
                        .id(1L)
                        .basePrice(100)
                        .byPerson(false)
                        .priceRules(new HashSet<>()).build())
                .build();

        mockAccommodationDto = AccommodationDto.builder()
                .id(1L)
                .userId(2L)
                .name("Vikendica")
                .tags(new HashSet<>())
                .images(new HashSet<>())
                .location(new LocationDto())
                .minGuestNum(1)
                .maxGuestNum(20)
                .availability(accommodationAvailabilityDto)
                .build();
    }

    @Test
    void makeReservation_whenValidData_returnReservation() {
        Mockito.when(reservationRepository.existsByAccommodationIdAndReservationOverlap(
                mockReservation.getAccommodationId(),
                mockReservation.getReservationStart(),
                mockReservation.getReservationEnd(),
                ReservationStatus.ACCEPTED)).thenReturn(false);

        Mockito.when(restUtil.userExistsById(1L)).thenReturn(true);
        Mockito.when(restUtil.getAccommodationById(1L)).thenReturn(mockAccommodationDto);
        Mockito.when(restUtil.getHostByAccommodationId(1L)).thenReturn(2L);

        Reservation reservation = reservationService.makeReservation(mockReservation, 1L);

        Assertions.assertEquals(ReservationStatus.PENDING, reservation.getReservationStatus());
        Assertions.assertEquals(2L, reservation.getHostId());
    }

    @Test
    void makeReservation_whenInvalidDate_throwException() {
        Mockito.when(reservationRepository.existsByAccommodationIdAndReservationOverlap(
                mockReservation.getAccommodationId(),
                mockReservation.getReservationStart(),
                mockReservation.getReservationEnd(),
                ReservationStatus.ACCEPTED)).thenReturn(false);

        Mockito.when(restUtil.userExistsById(1L)).thenReturn(true);
        Mockito.when(restUtil.getAccommodationById(1L)).thenReturn(mockAccommodationDto);
        Mockito.when(restUtil.getHostByAccommodationId(1L)).thenReturn(2L);

        Assertions.assertThrows(DateNotAvailableException.class, () -> {
            reservationService.makeReservation(invalidMockReservation, 1L);
        });
    }
}
