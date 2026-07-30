package com.example.shipping.config;

import com.example.shipping.model.ApiKeyRole;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class ApiKeyProperties {

    private Map<String, ApiKeyRole> apiKeys = Map.of();

    public Map<String, ApiKeyRole> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(Map<String, ApiKeyRole> apiKeys) {
        this.apiKeys = apiKeys;
    }
}
