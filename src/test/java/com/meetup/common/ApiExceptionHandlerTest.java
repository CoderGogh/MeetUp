package com.meetup.common;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {

    @RestController
    static class Probe {

        record Body(@NotBlank String nickname) {
        }

        @GetMapping("/api-error")
        void apiError() {
            throw new ApiException(ErrorCode.INVALID_TRANSITION, Map.of("currentState", "VOTING"));
        }

        @PostMapping("/validated")
        void validated(@Valid @RequestBody Body body) {
        }

        @GetMapping("/header")
        void header(@RequestHeader("Idempotency-Key") String key) {
        }

        @GetMapping("/boom")
        void boom() {
            throw new IllegalStateException("db password=hunter2");
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

    @Test
    void validationListsFields() throws Exception {
        mvc.perform(post("/validated").contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.details.fields.nickname").exists());
    }

    @Test
    void missingHeaderNamesHeader() throws Exception {
        mvc.perform(get("/header")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.header").value("Idempotency-Key"));
    }

    @Test
    void unreadableBodyIsBadRequest() throws Exception {
        mvc.perform(post("/validated").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void unexpectedErrorHidesCause() throws Exception {
        mvc.perform(get("/boom")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL"))
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL.message()));
    }
}
