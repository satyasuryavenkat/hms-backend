package com.app.hms.dto.response;

import com.app.hms.common.Enums.FollowUpStatus;
import java.time.*;

public record FollowUpResponse(
    Long id,
    PatientResponse patient,
    LocalDateTime visitDateTime,
    FollowUpStatus status,
    boolean reminderVisible,
    OffsetDateTime reminderActivatedAt,
    OffsetDateTime createdAt,
    String createdBy,
    OffsetDateTime remindedAt,
    String remindedBy) {}
