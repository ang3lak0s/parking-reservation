package hu.angel.parkingreservation.service;

import hu.angel.parkingreservation.entity.ParkingSpace;
import hu.angel.parkingreservation.entity.Requester;
import hu.angel.parkingreservation.entity.Reservation;
import hu.angel.parkingreservation.entity.ReservationStatus;
import hu.angel.parkingreservation.exception.InvalidReservationException;
import hu.angel.parkingreservation.exception.ReservationConflictException;
import hu.angel.parkingreservation.exception.ResourceNotFoundException;
import hu.angel.parkingreservation.repository.ParkingSpaceRepository;
import hu.angel.parkingreservation.repository.RequesterRepository;
import hu.angel.parkingreservation.repository.ReservationRepository;
import hu.angel.parkingreservation.exception.ReservationAlreadyCancelledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private ParkingSpaceRepository parkingSpaceRepository;
    private RequesterRepository requesterRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        parkingSpaceRepository = mock(ParkingSpaceRepository.class);
        requesterRepository = mock(RequesterRepository.class);

        reservationService = new ReservationService(
                reservationRepository,
                parkingSpaceRepository,
                requesterRepository
        );
    }

    @Test
    void createReservation_shouldCreateReservation_whenRequestIsValid() {

        ParkingSpace parkingSpace =
                new ParkingSpace("P-001", true);

        Requester requester =
                new Requester("John Doe");

        LocalDateTime startTime =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2026, 8, 20, 12, 0);

        when(parkingSpaceRepository.findById(1L))
                .thenReturn(Optional.of(parkingSpace));

        when(requesterRepository.findById(1L))
                .thenReturn(Optional.of(requester));

        when(reservationRepository
                .existsByParkingSpaceIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        eq(1L),
                        eq(ReservationStatus.ACTIVE),
                        eq(endTime),
                        eq(startTime)
                ))
                .thenReturn(false);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservation(
                1L,
                1L,
                startTime,
                endTime
        );

        assertNotNull(result);

        assertEquals(parkingSpace, result.getParkingSpace());
        assertEquals(requester, result.getRequester());

        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());

        assertEquals(
                ReservationStatus.ACTIVE,
                result.getStatus()
        );

        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_shouldRejectReservation_whenTimeRangeConflicts() {

        ParkingSpace parkingSpace =
                new ParkingSpace("P-001", true);

        Requester requester =
                new Requester("John Doe");

        LocalDateTime startTime =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2026, 8, 20, 12, 0);

        when(parkingSpaceRepository.findById(1L))
                .thenReturn(Optional.of(parkingSpace));

        when(requesterRepository.findById(1L))
                .thenReturn(Optional.of(requester));

        when(reservationRepository
                .existsByParkingSpaceIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        eq(1L),
                        eq(ReservationStatus.ACTIVE),
                        eq(endTime),
                        eq(startTime)
                ))
                .thenReturn(true);

        assertThrows(
                ReservationConflictException.class,
                () -> reservationService.createReservation(
                        1L,
                        1L,
                        startTime,
                        endTime
                )
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void createReservation_shouldAllowReservation_whenTimeRangeIsAdjacent() {

        ParkingSpace parkingSpace =
                new ParkingSpace("P-001", true);

        Requester requester =
                new Requester("John Doe");

        LocalDateTime startTime =
                LocalDateTime.of(2026, 8, 20, 12, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2026, 8, 20, 14, 0);

        when(parkingSpaceRepository.findById(1L))
                .thenReturn(Optional.of(parkingSpace));

        when(requesterRepository.findById(1L))
                .thenReturn(Optional.of(requester));

        when(reservationRepository
                .existsByParkingSpaceIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        eq(1L),
                        eq(ReservationStatus.ACTIVE),
                        eq(endTime),
                        eq(startTime)
                ))
                .thenReturn(false);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservation(
                1L,
                1L,
                startTime,
                endTime
        );

        assertNotNull(result);

        verify(reservationRepository)
                .save(any(Reservation.class));
    }

    @Test
    void createReservation_shouldAllowReservation_whenPreviousReservationIsCancelled() {

        ParkingSpace parkingSpace =
                new ParkingSpace("P-001", true);

        Requester requester =
                new Requester("John Doe");

        LocalDateTime startTime =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2026, 8, 20, 12, 0);

        when(parkingSpaceRepository.findById(1L))
                .thenReturn(Optional.of(parkingSpace));

        when(requesterRepository.findById(1L))
                .thenReturn(Optional.of(requester));

        when(reservationRepository
                .existsByParkingSpaceIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        eq(1L),
                        eq(ReservationStatus.ACTIVE),
                        eq(endTime),
                        eq(startTime)
                ))
                .thenReturn(false);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservation(
                1L,
                1L,
                startTime,
                endTime
        );

        assertEquals(ReservationStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getReservationsForParkingSpace_shouldReturnReservations_whenParkingSpaceExists() {

        ParkingSpace parkingSpace =
                new ParkingSpace("P-001", true);

        List<Reservation> reservations = List.of();

        when(parkingSpaceRepository.existsById(1L))
                .thenReturn(true);

        when(reservationRepository
                .findByParkingSpaceIdOrderByStartTime(1L))
                .thenReturn(reservations);

        List<Reservation> result =
                reservationService.getReservationsForParkingSpace(1L);

        assertEquals(reservations, result);

        verify(reservationRepository)
                .findByParkingSpaceIdOrderByStartTime(1L);
    }

    @Test
    void getReservationsForParkingSpace_shouldThrowException_whenParkingSpaceDoesNotExist() {

        when(parkingSpaceRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.getReservationsForParkingSpace(99L)
        );

        verifyNoInteractions(reservationRepository);
    }

    @Test
    void cancelReservation_shouldCancelReservation_whenReservationIsActive() {

        ParkingSpace parkingSpace =
                new ParkingSpace("P-001", true);

        Requester requester =
                new Requester("John Doe");

        Reservation reservation =
                new Reservation(
                        parkingSpace,
                        requester,
                        LocalDateTime.of(2026, 8, 20, 10, 0),
                        LocalDateTime.of(2026, 8, 20, 12, 0)
                );

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(1L);

        assertEquals(
                ReservationStatus.CANCELLED,
                reservation.getStatus()
        );
    }

    @Test
    void cancelReservation_shouldRejectCancellation_whenReservationIsAlreadyCancelled() {

        ParkingSpace parkingSpace =
                new ParkingSpace("P-001", true);

        Requester requester =
                new Requester("John Doe");

        Reservation reservation =
                new Reservation(
                        parkingSpace,
                        requester,
                        LocalDateTime.of(2026, 8, 20, 10, 0),
                        LocalDateTime.of(2026, 8, 20, 12, 0)
                );

        reservation.cancel();

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                ReservationAlreadyCancelledException.class,
                () -> reservationService.cancelReservation(1L)
        );
    }

    @Test
    void createReservation_shouldRejectReservation_whenRequesterDoesNotExist() {

        ParkingSpace parkingSpace =
                new ParkingSpace("P-001", true);

        LocalDateTime startTime =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2026, 8, 20, 12, 0);

        when(parkingSpaceRepository.findById(1L))
                .thenReturn(Optional.of(parkingSpace));

        when(requesterRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.createReservation(
                        1L,
                        99L,
                        startTime,
                        endTime
                )
        );

        verifyNoInteractions(reservationRepository);
    }

    @Test
    void createReservation_shouldRejectReservation_whenStartTimeEqualsEndTime() {

        LocalDateTime time =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        assertThrows(
                InvalidReservationException.class,
                () -> reservationService.createReservation(
                        1L,
                        1L,
                        time,
                        time
                )
        );

        verifyNoInteractions(
                parkingSpaceRepository,
                requesterRepository,
                reservationRepository
        );
    }

    @Test
    void createReservation_shouldRejectReservation_whenStartTimeIsAfterEndTime() {

        LocalDateTime startTime =
                LocalDateTime.of(2026, 8, 20, 14, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2026, 8, 20, 12, 0);

        assertThrows(
                InvalidReservationException.class,
                () -> reservationService.createReservation(
                        1L,
                        1L,
                        startTime,
                        endTime
                )
        );

        verifyNoInteractions(
                parkingSpaceRepository,
                requesterRepository,
                reservationRepository
        );
    }

    @Test
    void createReservation_shouldRejectReservation_whenParkingSpaceDoesNotExist() {

        LocalDateTime startTime =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2026, 8, 20, 12, 0);

        when(parkingSpaceRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.createReservation(
                        99L,
                        1L,
                        startTime,
                        endTime
                )
        );

        verifyNoInteractions(
                requesterRepository,
                reservationRepository
        );
    }

    @Test
    void createReservation_shouldRejectReservation_whenParkingSpaceIsInactive() {

        ParkingSpace parkingSpace =
                new ParkingSpace("P-001", false);

        LocalDateTime startTime =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2026, 8, 20, 12, 0);

        when(parkingSpaceRepository.findById(1L))
                .thenReturn(Optional.of(parkingSpace));

        assertThrows(
                InvalidReservationException.class,
                () -> reservationService.createReservation(
                        1L,
                        1L,
                        startTime,
                        endTime
                )
        );

        verifyNoInteractions(
                requesterRepository,
                reservationRepository
        );
    }
}