package com.app.hms.service;

import com.app.hms.common.*;
import com.app.hms.common.Enums.FollowUpStatus;
import com.app.hms.dto.request.FollowUpRequest;
import com.app.hms.dto.response.FollowUpResponse;
import com.app.hms.entity.FollowUp;
import com.app.hms.mapper.PatientMapper;
import com.app.hms.repository.FollowUpRepository;
import java.time.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowUpServiceImpl implements FollowUpService {
  static final ZoneId HOSPITAL_ZONE = ZoneId.of("Asia/Kolkata");

  private final FollowUpRepository repository;
  private final PatientService patients;
  private final PatientMapper patientMapper;

  @Override
  @Transactional
  public FollowUpResponse create(FollowUpRequest request, String createdBy) {
    LocalDateTime visitDateTime =
        LocalDateTime.of(request.followUpDate(), request.followUpTime());
    if (!visitDateTime.isAfter(LocalDateTime.now(HOSPITAL_ZONE))) {
      throw new BadRequestException("Follow-up date and time must be in the future");
    }

    FollowUp followUp = new FollowUp();
    followUp.setPatient(patients.findEntityById(request.patientId()));
    followUp.setVisitDateTime(visitDateTime);
    followUp.setCreatedBy(createdBy);
    if (request.followUpDate().equals(LocalDate.now(HOSPITAL_ZONE).plusDays(1))) {
      followUp.setReminderVisible(true);
      followUp.setReminderActivatedAt(OffsetDateTime.now(HOSPITAL_ZONE));
    }
    return response(repository.save(followUp));
  }

  @Override
  public List<FollowUpResponse> reminders() {
    LocalDate reminderDate = LocalDate.now(HOSPITAL_ZONE).plusDays(1);
    return repository.findByReminderVisibleTrueOrderByVisitDateTimeAsc().stream()
        .filter(followUp -> followUp.getVisitDateTime().toLocalDate().equals(reminderDate))
        .map(this::response)
        .toList();
  }

  @Override
  @Transactional
  public FollowUpResponse markReminded(Long id, String remindedBy) {
    FollowUp followUp =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Follow-up not found"));
    if (!followUp.isReminderVisible()) {
      throw new BadRequestException("This follow-up is not in today's reminder window");
    }
    if (followUp.getStatus() == FollowUpStatus.REMINDED) return response(followUp);
    followUp.setStatus(FollowUpStatus.REMINDED);
    followUp.setRemindedAt(OffsetDateTime.now(HOSPITAL_ZONE));
    followUp.setRemindedBy(remindedBy);
    return response(repository.save(followUp));
  }

  @Override
  @Transactional
  public void refreshReminderWindow() {
    LocalDate reminderDate = LocalDate.now(HOSPITAL_ZONE).plusDays(1);
    OffsetDateTime now = OffsetDateTime.now(HOSPITAL_ZONE);
    repository
        .findAll()
        .forEach(
            followUp -> {
              boolean visible = followUp.getVisitDateTime().toLocalDate().equals(reminderDate);
              if (visible && !followUp.isReminderVisible()) {
                followUp.setReminderActivatedAt(now);
              }
              followUp.setReminderVisible(visible);
            });
  }

  private FollowUpResponse response(FollowUp followUp) {
    return new FollowUpResponse(
        followUp.getId(),
        patientMapper.toResponse(followUp.getPatient()),
        followUp.getVisitDateTime(),
        followUp.getStatus(),
        followUp.isReminderVisible(),
        followUp.getReminderActivatedAt(),
        followUp.getCreatedAt(),
        followUp.getCreatedBy(),
        followUp.getRemindedAt(),
        followUp.getRemindedBy());
  }
}
