package com.flight.auth.dto;

import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Enabled status is required")
    private Boolean enabled;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
