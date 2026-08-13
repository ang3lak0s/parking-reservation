package hu.angel.parkingreservation.controller;

import hu.angel.parkingreservation.dto.CreateReservationRequest;
import hu.angel.parkingreservation.dto.ReservationResponse;
import hu.angel.parkingreservation.entity.Reservation;
import hu.angel.parkingreservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request
    ) {
        Reservation reservation = reservationService.createReservation(
                request.getParkingSpaceId(),
                request.getRequesterId(),
                request.getStartTime(),
                request.getEndTime()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ReservationResponse.fromEntity(reservation));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long reservationId
    ) {
        reservationService.cancelReservation(reservationId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/parking-space/{parkingSpaceId}")
    public ResponseEntity<List<ReservationResponse>> getReservationsForParkingSpace(
            @PathVariable Long parkingSpaceId
    ) {
        List<ReservationResponse> reservations =
                reservationService.getReservationsForParkingSpace(parkingSpaceId)
                        .stream()
                        .map(ReservationResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(reservations);
    }
}