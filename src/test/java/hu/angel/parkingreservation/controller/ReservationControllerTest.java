package hu.angel.parkingreservation.controller;

import hu.angel.parkingreservation.exception.GlobalExceptionHandler;
import hu.angel.parkingreservation.exception.ReservationConflictException;
import hu.angel.parkingreservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(GlobalExceptionHandler.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void createReservation_shouldReturnConflict_whenReservationConflicts()
            throws Exception {

        doThrow(new ReservationConflictException(
                "Parking space is already reserved for the requested time range."
        ))
                .when(reservationService)
                .createReservation(
                        any(),
                        any(),
                        any(),
                        any()
                );

        String requestBody = """
                {
                  "parkingSpaceId": 1,
                  "requesterId": 1,
                  "startTime": "2026-08-20T10:00:00",
                  "endTime": "2026-08-20T12:00:00"
                }
                """;

        mockMvc.perform(
                        post("/api/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "Parking space is already reserved for the requested time range."
                ));
    }

    @Test
    void createReservation_shouldReturnBadRequest_whenRequiredFieldsAreMissing()
            throws Exception {

        String requestBody = """
            {}
            """;

        mockMvc.perform(
                        post("/api/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.parkingSpaceId")
                        .value("Parking space ID is required."))
                .andExpect(jsonPath("$.validationErrors.requesterId")
                        .value("Requester ID is required."))
                .andExpect(jsonPath("$.validationErrors.startTime")
                        .value("Start time is required."))
                .andExpect(jsonPath("$.validationErrors.endTime")
                        .value("End time is required."));
    }
}