package hu.angel.parkingreservation.dto;

import hu.angel.parkingreservation.entity.Reservation;
import hu.angel.parkingreservation.entity.ReservationStatus;

import java.time.LocalDateTime;

public class ReservationResponse {

    private final Long id;
    private final Long parkingSpaceId;
    private final Long requesterId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final ReservationStatus status;
    private final LocalDateTime createdAt;

    public ReservationResponse(
            Long id,
            Long parkingSpaceId,
            Long requesterId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            ReservationStatus status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.parkingSpaceId = parkingSpaceId;
        this.requesterId = requesterId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static ReservationResponse fromEntity(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getParkingSpace().getId(),
                reservation.getRequester().getId(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getParkingSpaceId() {
        return parkingSpaceId;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}