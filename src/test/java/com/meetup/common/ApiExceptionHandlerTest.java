package com.meetup.common;

import java.util.Map;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
