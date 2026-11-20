package com.meetup.common;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {

    @RestController
    static class Probe {

        @GetMapping("/api-error")
        void apiError() {
            throw new ApiException(ErrorCode.INVALID_TRANSITION, Map.of("currentState", "VOTING"));
        }
    }

    final MockMvc mvc = MockMvcBuilders.standaloneSetup(new Probe())
            .setControllerAdvice(new ApiExceptionHandler()).build();

    @Test
    void apiExceptionKeepsCodeAndDetails() throws Exception {
        mvc.perform(get("/api-error")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_TRANSITION"))
                .andExpect(jsonPath("$.details.currentState").value("VOTING"));
    }
}
