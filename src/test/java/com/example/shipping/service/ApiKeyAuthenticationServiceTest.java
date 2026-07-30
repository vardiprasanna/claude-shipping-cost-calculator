package com.example.shipping.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shipping.model.ApiKeyRole;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiKeyAuthenticationServiceTest {

    private final ApiKeyAuthenticationService service =
            new ApiKeyAuthenticationService(Map.of("admin-key-123", ApiKeyRole.ADMIN));

    @Test
    @DisplayName("The one where a configured ADMIN key resolves to the ADMIN role")
    void resolvesConfiguredAdminKeyToAdminRole() {
        assertThat(service.roleFor("admin-key-123")).contains(ApiKeyRole.ADMIN);
    }

    @Test
    @DisplayName("The one where the ADMIN role implies the USER role too, since ADMIN is a superset")
    void adminRoleImpliesUserRole() {
        assertThat(service.impliedRoles(ApiKeyRole.ADMIN)).containsExactlyInAnyOrder(ApiKeyRole.ADMIN, ApiKeyRole.USER);
    }

    @Test
    @DisplayName("The one where the USER role implies only itself (counter-example)")
    void userRoleImpliesOnlyItself() {
        assertThat(service.impliedRoles(ApiKeyRole.USER)).containsExactly(ApiKeyRole.USER);
    }
}
