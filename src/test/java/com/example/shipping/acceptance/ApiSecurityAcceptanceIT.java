package com.example.shipping.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("API Security")
class ApiSecurityAcceptanceIT {

    @Autowired
    private MockMvcTester mvc;

    @Nested
    @DisplayName("Must require a valid, recognized API key via the X-API-Key header for every request "
            + "to a protected endpoint, rejecting missing or unrecognized keys with 401 Unauthorized")
    class RequiresValidRecognizedApiKey {

        @ParameterizedTest(name = "The one where the X-API-Key header is {0} and the response is {1}")
        @CsvSource({
                "NONE,            401",
                "not-a-real-key,  401",
                "user-test-key,   200",
                "admin-test-key,  200",
        })
        void requiresValidRecognizedApiKeyForProtectedEndpoint(String apiKeyHeader, int expectedStatus) {
            var request = mvc.post().uri("/api/shipping/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": 1.000, "zone": "DOMESTIC", "orderTotal": 10.00 }
                            """);

            if (!"NONE".equals(apiKeyHeader)) {
                request = request.header("X-API-Key", apiKeyHeader);
            }

            MvcTestResult result = request.exchange();

            assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus);
        }
    }

    @Nested
    @DisplayName("Must restrict the admin-only endpoint (GET /api/admin/rates) to ADMIN-role keys, "
            + "rejecting an authenticated but under-privileged USER key with 403 Forbidden")
    class RestrictsAdminEndpointToAdminRole {

        @Test
        @DisplayName("The one where a valid ADMIN key requests GET /api/admin/rates")
        void adminKeyCanAccessAdminEndpoint() {
            assertThat(mvc.get().uri("/api/admin/rates")
                    .header("X-API-Key", "admin-test-key"))
                    .hasStatusOk();
        }

        @Test
        @DisplayName("The one where a valid USER key (authenticated, but wrong role) requests GET "
                + "/api/admin/rates (counter-example)")
        void userKeyIsForbiddenFromAdminEndpoint() {
            assertThat(mvc.get().uri("/api/admin/rates")
                    .header("X-API-Key", "user-test-key"))
                    .hasStatus(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("Must exempt Swagger/OpenAPI documentation from authentication")
    class ExemptsSwaggerDocumentationFromAuthentication {

        @Test
        @DisplayName("The one where an unauthenticated request to /swagger-ui.html succeeds, "
                + "no API key required")
        void unauthenticatedSwaggerUiRequestSucceeds() {
            MvcTestResult result = mvc.get().uri("/swagger-ui.html").exchange();

            assertThat(result.getResponse().getStatus()).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("The one where the same unauthenticated request instead targets "
                + "/api/shipping/calculate and is rejected with 401 Unauthorized (counter-example)")
        void unauthenticatedShippingCalculateRequestIsRejected() {
            assertThat(mvc.post().uri("/api/shipping/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": 1.000, "zone": "DOMESTIC", "orderTotal": 10.00 }
                            """))
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }
    }
}
