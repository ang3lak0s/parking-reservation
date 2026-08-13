package hu.angel.parkingreservation.exception;

public class ReservationAlreadyCancelledException extends RuntimeException {

    public ReservationAlreadyCancelledException(String message) {
        super(message);
    }
}