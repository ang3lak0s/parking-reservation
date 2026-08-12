package hu.angel.parkingreservation.repository;

import hu.angel.parkingreservation.entity.ParkingSpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {

    Optional<ParkingSpace> findByIdentifier(String identifier);
}