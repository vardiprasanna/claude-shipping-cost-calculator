package com.example.shipping.service;

import com.example.shipping.model.ApiKeyRole;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ApiKeyAuthenticationService {

    private final Map<String, ApiKeyRole> keyRoles;

    public ApiKeyAuthenticationService(Map<String, ApiKeyRole> keyRoles) {
        this.keyRoles = keyRoles;
    }

    public Optional<ApiKeyRole> roleFor(String apiKey) {
        return Optional.ofNullable(keyRoles.get(apiKey));
    }

    public Set<ApiKeyRole> impliedRoles(ApiKeyRole role) {
        if (role == ApiKeyRole.ADMIN) {
            return EnumSet.of(ApiKeyRole.ADMIN, ApiKeyRole.USER);
        }
        return EnumSet.of(role);
    }
}
