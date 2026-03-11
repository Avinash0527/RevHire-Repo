package com.example.revhirehiringplatform.dto.request;

import com.example.revhirehiringplatform.model.Application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationStatusRequest {
    @NotNull(message = "Status is required")
    private ApplicationStatus status;
}
