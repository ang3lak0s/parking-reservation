package hu.angel.parkingreservation.repository;

import hu.angel.parkingreservation.entity.Requester;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequesterRepository extends JpaRepository<Requester, Long> {
}