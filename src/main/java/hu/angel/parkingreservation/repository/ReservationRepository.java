package hu.angel.parkingreservation.repository;

import hu.angel.parkingreservation.entity.Reservation;
import hu.angel.parkingreservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByParkingSpaceIdOrderByStartTime(Long parkingSpaceId);

    boolean existsByParkingSpaceIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Long parkingSpaceId,
            ReservationStatus status,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}