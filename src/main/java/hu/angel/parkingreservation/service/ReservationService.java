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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final RequesterRepository requesterRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ParkingSpaceRepository parkingSpaceRepository,
            RequesterRepository requesterRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.parkingSpaceRepository = parkingSpaceRepository;
        this.requesterRepository = requesterRepository;
    }

    @Transactional
    public Reservation createReservation(
            Long parkingSpaceId,
            Long requesterId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        validateTimeRange(startTime, endTime);

        ParkingSpace parkingSpace = parkingSpaceRepository.findById(parkingSpaceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parking space not found: " + parkingSpaceId
                ));

        if (!parkingSpace.isActive()) {
            throw new InvalidReservationException(
                    "Parking space is not active: " + parkingSpaceId
            );
        }

        Requester requester = requesterRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requester not found: " + requesterId
                ));

        boolean conflict = reservationRepository
                .existsByParkingSpaceIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        parkingSpaceId,
                        ReservationStatus.ACTIVE,
                        endTime,
                        startTime
                );

        if (conflict) {
            throw new ReservationConflictException(
                    "Parking space is already reserved for the requested time range."
            );
        }

        Reservation reservation = new Reservation(
                parkingSpace,
                requester,
                startTime,
                endTime
        );

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsForParkingSpace(Long parkingSpaceId) {

        if (!parkingSpaceRepository.existsById(parkingSpaceId)) {
            throw new ResourceNotFoundException(
                    "Parking space not found: " + parkingSpaceId
            );
        }

        return reservationRepository
                .findByParkingSpaceIdOrderByStartTime(parkingSpaceId);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found: " + reservationId
                ));

        reservation.cancel();

        reservationRepository.save(reservation);
    }

    private void validateTimeRange(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (startTime == null || endTime == null) {
            throw new InvalidReservationException(
                    "Start time and end time are required."
            );
        }

        if (!startTime.isBefore(endTime)) {
            throw new InvalidReservationException(
                    "Start time must be before end time."
            );
        }
    }
}