package com.app.hms.service;

import com.app.hms.dto.request.FollowUpRequest;
import com.app.hms.dto.response.FollowUpResponse;
import java.util.List;

public interface FollowUpService {
  FollowUpResponse create(FollowUpRequest request, String createdBy);

  List<FollowUpResponse> reminders();

  FollowUpResponse markReminded(Long id, String remindedBy);

  void refreshReminderWindow();
}
