package buky.example.reservationsservice.service;

import buky.example.reservationsservice.dto.*;
import buky.example.reservationsservice.enumerations.ReservationStatus;
import buky.example.reservationsservice.exceptions.ActionNotPermittedException;
import buky.example.reservationsservice.exceptions.DateNotAvailableException;
import buky.example.reservationsservice.exceptions.InvalidGuestNumberException;
import buky.example.reservationsservice.exceptions.NotFoundException;
import buky.example.reservationsservice.messaging.messages.ReservationStatusChangedMessage;
import buky.example.reservationsservice.messaging.messages.UserDeletionRequestMessage;
import buky.example.reservationsservice.messaging.messages.UserDeletionResponseMessage;
import buky.example.reservationsservice.messaging.producers.KafkaProducer;
import buky.example.reservationsservice.model.Reservation;
import buky.example.reservationsservice.model.enumerations.Role;
import buky.example.reservationsservice.repository.ReservationRepository;
import buky.example.reservationsservice.util.RestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestUtil restUtil;
    private final KafkaProducer publisher;


    public Reservation makeReservation(Reservation reservation, Long userId) {
        validateReservation(reservation);
        setUser(reservation, userId);
        processByAccommodation(reservation);
        reservationRepository.save(reservation);

        sendNotificationToHost(userId, reservation);

        return reservation;
    }

    public Reservation cancel(Long id, Long userId) {
        var reservation = reservationRepository.findById(id).orElseThrow(NotFoundException::new);
        checkCancellationPermitted(reservation, userId);
        reservation.setReservationStatus(ReservationStatus.CANCELED);
        reservationRepository.save(reservation);

        sendNotificationToHost(userId, reservation);

        return reservation;
    }

    private void sendNotificationToHost(Long userId, Reservation reservation) {
        Long hostId = restUtil.getHostByAccommodationId(reservation.getAccommodationId());

        publisher.send("reservation-status-changed",
                new ReservationStatusChangedMessage(userId, hostId, reservation.getId(), reservation.getReservationStatus()));
    }

    //host cancells
    public Reservation withdraw(Long id, Long userId) {
        var reservation = reservationRepository.findById(id).orElseThrow(NotFoundException::new);
        checkWithdrawalPermitted(reservation, userId);
        reservation.setReservationStatus(ReservationStatus.WITHDRAWN);

        sendNotificationToGuest(userId, reservation);

        return reservationRepository.save(reservation);
    }

    public Reservation acceptReservation(Long id, Long userId) {
        var reservation = reservationRepository.findById(id).orElseThrow(NotFoundException::new);
        checkAcceptPermitted(reservation, userId);
        //decline all overlap
        var cancelledReservationsNum = cancelOthersOverlapping(reservation);

        reservation.setReservationStatus(ReservationStatus.ACCEPTED);
        //TODO notify denied? mozda promeniti query... cancelledReservationsNum?

        sendNotificationToGuest(userId, reservation);
        return reservationRepository.save(reservation);
    }

    public Reservation declineReservation(Long id, Long userId) {
        var reservation = reservationRepository.findById(id).orElseThrow(NotFoundException::new);
        checkStatus(reservation, ReservationStatus.PENDING);
        checkUserAccommodationOwner(reservation, userId);
        reservation.setReservationStatus(ReservationStatus.DENIED);

        sendNotificationToGuest(userId, reservation);
        return reservationRepository.save(reservation);
    }

    private void sendNotificationToGuest(Long userId, Reservation reservation) {
        publisher.send("reservation-status-changed",
                new ReservationStatusChangedMessage(userId, reservation.getUserId(), reservation.getId(), reservation.getReservationStatus()));
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

    public List<Reservation> findAllReservationsByAccommodationIdAndStatus(Long id, ReservationStatus reservationStatus) {
        return reservationRepository.findByAccommodationIdAndReservationStatus(id, reservationStatus);
    }

    public void dumpInvalid(Reservation reservation, Long userId) {
        reservation.setUserId(userId);
        reservation.setReservationStatus(ReservationStatus.INVALID);
        reservationRepository.save(reservation);
    }

    private void validateReservation(Reservation reservation) {
        //invalid je svakako, dok ne prodje sve provere...
        reservation.setReservationStatus(ReservationStatus.INVALID);
        //TODO additional checks if needed?
        if (reservation.getReservationStart().isAfter(reservation.getReservationEnd()))
            throw new DateNotAvailableException("End date cannot be before start date!");
        //Da li je u proslosti
        if (reservation.getReservationStart().atStartOfDay().isBefore(LocalDate.now().atStartOfDay()))
            throw new DateNotAvailableException("Cannot reserve accommodation in the past!");

        if (!isReservationAvailable(reservation))
            throw new DateNotAvailableException();
    }

    private boolean isReservationAvailable(Reservation reservation) {
        return !reservationRepository.existsByAccommodationIdAndReservationOverlap(
                reservation.getAccommodationId(),
                reservation.getReservationStart(),
                reservation.getReservationEnd(),
                ReservationStatus.ACCEPTED);
    }

    private void setReservationPrice(AccommodationDto accommodationDto, Reservation reservation) {
        var total = calculateTotalPrice(accommodationDto, reservation.getReservationStart(), reservation.getReservationEnd());
        var guestNum = reservation.getGuestsNum() == null ? 1.0 : reservation.getGuestsNum();
        reservation.setTotalPrice(total);
        reservation.setPriceByGuest(total / Math.ceil(reservation.getGuestsNum()));
    }

    private double calculateTotalPrice(AccommodationDto accommodationDto, LocalDate reservationStart, LocalDate reservationEnd) {

        var days = dateRangeToSet(reservationStart, reservationEnd);
        var priceList = accommodationDto.getAvailability().getPrice();

        var total = days
                .stream()
                .map(day -> calculateDayPrice(priceList, day))
                .reduce(0.0, Double::sum);

        return total;
    }

    private double calculateDayPrice(PriceDto priceList, LocalDate day) {
        //if any rule applies return
        for (PriceRuleDto rule : priceList.getPriceRules()) {
            if (patternRuleAppliesToDate(rule.getPatternPeriod(), day))
                return rule.getSpecialPrice();
            if (rangeRuleAppliesToDate(rule.getRangePeriod(), day)) {
                return rule.getSpecialPrice();
            }
        }
        //no rule applies for the day => return BasePrice
        return priceList.getBasePrice();
    }

    private boolean patternRuleAppliesToDate(PatternPeriodDto patternPeriod, LocalDate day) {
        return patternPeriod.getDayOfWeek().contains(day.getDayOfWeek());
    }

    private boolean rangeRuleAppliesToDate(RangePeriodDto rangePeriod, LocalDate day) {
        return !day.isBefore(rangePeriod.getStartDate()) && !day.isAfter(rangePeriod.getEndDate());
    }

    /**
     * Tri scenaria:
     * 1 - neki od range u potpunosti obuhvata rezervaciju
     * 2 - neka kombinacija patterna u potpunosti obuhvata rezervaciju
     * 3 - ni range ni pattern ne pokrivaju samostalno ali njihova kombinacija pokriva rezervaciju:
     * npr:    * 4. po redu range se zavrsava u cetvrtak 03.08.2023
     * * 2. po redu pattern kaze da je slobodno svakog petka
     * * 3. po redu pattern kaze da je slobodno vikendom
     * * 5. po redu range kaze da je slobodno od ponedljeka 07.08.2023.
     * => rezervacija 01.08 - 10.08 je validna...
     */
    private boolean accommodationAvailable(AccommodationDto accommodationDto, LocalDate reservationStart, LocalDate reservationEnd) {
        if (reservationEnd == null || reservationStart == null || accommodationDto == null)
            throw new DateNotAvailableException();

        //PRE scenario 3 - rangovi koji se potencijalno mogu "zakrpiti" patternima...
        Set<LocalDate> rangeDatesOfInterest = new HashSet<>();

        //scenario 1
        var rangePeriods = accommodationDto.getAvailability().getAllRangePeriods();
        if (!rangePeriods.isEmpty()) {
            for (RangePeriodDto range : rangePeriods) {
                var rangeStart = range.getStartDate();
                var rangeEnd = range.getEndDate();
                boolean startOverlap = !reservationStart.isBefore(rangeStart);
                boolean endOverlap = !reservationEnd.isAfter(rangeEnd);
                if (startOverlap && endOverlap) {
                    return true;
                }
                //ne upada u ceo range ali potencijalno se moze zakrpiti kasnije...

                //startOverlap znaci da se rez[pocetak] uklapa u termin ali rey[kraj] ispada iz termina...
                // => interes nam je da od rez[pocetak] do kraja termina ubacimo kao interes datume

                if (startOverlap) {
                    rangeDatesOfInterest.addAll(dateRangeToSet(reservationStart, rangeEnd));
                    break;
                }
                //endOverlap znaci da slobodni termini pokrivaju neki zavrsni deo rezervacije, ali fale dani pocetka...
                // => interes nam je da od termin[start] do rez[end] ubacimo kao interes datume
                if (endOverlap) {
                    rangeDatesOfInterest.addAll(dateRangeToSet(rangeStart, reservationEnd));
                }
            }

        }

        //scenario 2
        var patternPeriods = accommodationDto.getAvailability().getAllPatternPeriods();
        Set<LocalDate> reservationDates = dateRangeToSet(reservationStart, reservationEnd);
        Set<LocalDate> patternDates = new HashSet<>();

        if (!patternPeriods.isEmpty()) {
            patternDates = patternPeriods
                    .stream()
                    .map(patternPeriod ->
                            patternPeriod.getPeriodDates(reservationStart, reservationEnd))
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());

            if (patternDates.containsAll(reservationDates))
                return true;
        }
        //scenario 3
        if (!rangeDatesOfInterest.isEmpty()) {
            if (!patternDates.isEmpty())
                rangeDatesOfInterest.addAll(patternDates);
            return rangeDatesOfInterest.containsAll(reservationDates);
        }
        return false;
    }

    private Set<LocalDate> dateRangeToSet(LocalDate start, LocalDate end) {
        if (end.isBefore(start))
            throw new DateNotAvailableException();

        Set<LocalDate> dateSequence = new HashSet<>();

        LocalDate curr = start;
        while (!curr.isAfter(end)) {
            dateSequence.add(curr);
            curr = curr.plusDays(1);
        }

        return dateSequence;
    }

    private void setReservationStatus(AccommodationDto accommodation, Reservation reservation) {
        if (accommodation.isAutoApproveReservation()) {
            reservation.setReservationStatus(ReservationStatus.ACCEPTED);
            //slucaj ako nekada nije bio automatski pa je podeseno naknadno... stare pending treba obraditi...
            cancelOthersOverlapping(reservation);
        } else
            reservation.setReservationStatus(ReservationStatus.PENDING);
    }

    private void validateGuestCount(AccommodationDto accommodation, Reservation reservation) {
        var min = reservation.getGuestsNum() >= accommodation.getMinGuestNum();
        var max = reservation.getGuestsNum() <= accommodation.getMaxGuestNum();

        if (!min)
            throw new InvalidGuestNumberException(String.format("This Accommodation requires a minimum of %d guests.", accommodation.getMinGuestNum()));
        if (!max)
            throw new InvalidGuestNumberException(String.format("This Accommodation accepts a maximum of %d guests.", accommodation.getMaxGuestNum()));

    }

    private void setUser(Reservation reservation, Long userId) {
        if (restUtil.userExistsById(reservation.getUserId()))
            reservation.setUserId(userId);
        else
            throw new NotFoundException(String.format("User with id %d not found...", userId));
    }

    private void processByAccommodation(Reservation reservation) {

        var accommodation = restUtil.getAccommodationById(reservation.getAccommodationId());

        if (!accommodationAvailable(accommodation, reservation.getReservationStart(), reservation.getReservationEnd()))
            throw new DateNotAvailableException();

        validateGuestCount(accommodation, reservation);
        setReservationPrice(accommodation, reservation);
        setReservationStatus(accommodation, reservation);
    }

    private void checkCancellationPermitted(Reservation reservation, Long userId) {

        checkUserIsReservationOwner(reservation, userId);
        checkCancelWithdrawalStatus(reservation);

        var permitted = reservation.getReservationStart()
                .atStartOfDay()
                .minusDays(1)
                .isAfter(LocalDate.now().atStartOfDay());
        if (!permitted)
            throw new ActionNotPermittedException("Action not permitted! Minimum of one day difference required to cancel!");
    }

    private void checkWithdrawalPermitted(Reservation reservation, Long userId) {
        checkUserAccommodationOwner(reservation, userId);
        checkCancelWithdrawalStatus(reservation);
    }

    private void checkAcceptPermitted(Reservation reservation, Long userId) {
        checkUserAccommodationOwner(reservation, userId);
        checkStatus(reservation, ReservationStatus.PENDING);
    }

    private void checkCancelWithdrawalStatus(Reservation reservation) {
        if (!(reservation.getReservationStatus() == ReservationStatus.ACCEPTED ||
                reservation.getReservationStatus() == ReservationStatus.PENDING))
            throw new ActionNotPermittedException(
                    String.format("Action not permitted for Reservation[status] %s.", reservation.getReservationStatus().toString()));
    }

    private void checkStatus(Reservation reservation, ReservationStatus statusToCheck) {
        if (!reservation.getReservationStatus().equals(statusToCheck))
            throw new ActionNotPermittedException(
                    String.format("Action not permitted for Reservation[status] %s.", reservation.getReservationStatus().toString()));
    }

    private void checkUserIsReservationOwner(Reservation reservation, Long userId) {
        if (!reservation.getUserId().equals(userId))
            throw new ActionNotPermittedException(String.format("Action not permitted for user[id]: %d.", userId));
    }

    private void checkUserAccommodationOwner(Reservation reservation, Long userId) {
        var accommodation = restUtil.getAccommodationById(reservation.getAccommodationId());
        if (!accommodation.getUserId().equals(userId))
            throw new ActionNotPermittedException(String.format("Action not permitted for user[id]: %d.", userId));
    }

    private int cancelOthersOverlapping(Reservation reservation) {
        return reservationRepository
                .updateReservationStatusByDateOverlap(
                        ReservationStatus.DENIED,
                        reservation.getAccommodationId(),
                        ReservationStatus.PENDING,
                        reservation.getReservationStart(),
                        reservation.getReservationEnd());
    }

    public Boolean isUserHasPreviousReservations(Long userId, Long accId) {
        return reservationRepository.existsByUserIdAndAccommodationIdAndReservationStatusNot(
                userId,
                accId,
                ReservationStatus.CANCELED
        );
    }

    public Boolean isUserStayedIn(Long userId, Long accommodationId) {
        return reservationRepository.existsByUserIdAndAccommodationIdAndReservationEndBeforeAndReservationStatusIn(
                userId,
                accommodationId,
                LocalDate.now().plusDays(1),      //zavrsava se najmanje danas ili zavrseno pre...
                List.of(ReservationStatus.ACCEPTED, ReservationStatus.IN_PROGRESS, ReservationStatus.DONE)
        );
    }

    public void userDeletionRequest(UserDeletionRequestMessage message) {
        var response = UserDeletionResponseMessage
                .builder()
                .userId(message.getUserId())
                .role(message.getUserType())
                .permitted(false)
                .build();
        if (message.getUserType().equals(Role.HOST)) {
            List<Long> accommodationIds = restUtil.getAccommodationIdsByOwner(message.getUserId());
            if (!accommodationIds.isEmpty() && !checkReservationExistForHost(message, accommodationIds)) {
                performOwnerDeletion(accommodationIds);
                response.setPermitted(true);
            }
            publisher.send("user-deletion-permission-topic", response);
            return;
        }
        //else is guest
        if (!checkReservationExistForGuest(message)) {
            performGuestDeletion(message.getUserId());
            response.setPermitted(true);
        }
        publisher.send("user-deletion-permission-topic", response);
    }

    private void performOwnerDeletion(List<Long> accommodationIds) {
        reservationRepository.deleteByAccommodationIdIn(accommodationIds);
    }

    private void performGuestDeletion(Long userId) {
        reservationRepository.deleteByUserId(userId);
    }

    private boolean checkReservationExistForHost(UserDeletionRequestMessage message, List<Long> accommodationIds) {
        return reservationRepository.existsByAccommodationIdInAndReservationStatusIn(
                accommodationIds,
                List.of(ReservationStatus.ACCEPTED,
                        ReservationStatus.IN_PROGRESS)
        );
    }

    private boolean checkReservationExistForGuest(UserDeletionRequestMessage message) {
        return reservationRepository
                .existsByUserIdAndReservationStatusIn(
                        message.getUserId(),
                        List.of(ReservationStatus.ACCEPTED,
                                ReservationStatus.IN_PROGRESS)
                );
    }

    public List<Long> getUnavailableAccommodations(LocalDate start, LocalDate end) {
        return reservationRepository.findAllUnavailable(start, end, List.of(ReservationStatus.IN_PROGRESS,
                ReservationStatus.ACCEPTED));
    }

    public List<Reservation> getForHost(Long userId, boolean onlyPending) {
        List<Long> accommodationIds = restUtil.getAccommodationIdsByOwner(userId);
        if (accommodationIds.isEmpty())
            return List.of();

        if (onlyPending)
            return reservationRepository.findByAccommodationIdIn(accommodationIds);

        return reservationRepository.findByAccommodationIdInAndReservationStatusIn(accommodationIds, List.of(ReservationStatus.PENDING));
    }
}
