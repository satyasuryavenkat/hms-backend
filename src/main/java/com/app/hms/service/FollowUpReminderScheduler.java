package com.app.hms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowUpReminderScheduler {
  private final FollowUpService service;

  @EventListener(ApplicationReadyEvent.class)
  public void initializeReminderWindow() {
    service.refreshReminderWindow();
  }

  @Scheduled(cron = "${app.followups.reminder-cron:0 0 */3 * * *}", zone = "Asia/Kolkata")
  public void refreshEveryThreeHours() {
    service.refreshReminderWindow();
  }
}
