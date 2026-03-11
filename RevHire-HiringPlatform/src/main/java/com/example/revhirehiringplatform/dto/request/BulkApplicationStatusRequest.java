package com.example.revhirehiringplatform.dto.request;

import com.example.revhirehiringplatform.model.Application.ApplicationStatus;
import lombok.Data;
import java.util.List;

@Data
public class BulkApplicationStatusRequest {
    private List<Long> applicationIds;
    private ApplicationStatus status;
    private String comment;
}
