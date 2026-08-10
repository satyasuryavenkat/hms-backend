package com.app.hms.repository;

import com.app.hms.common.Enums.FollowUpStatus;
import com.app.hms.entity.FollowUp;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
  List<FollowUp> findByStatus(FollowUpStatus status);

  List<FollowUp> findByReminderVisibleTrueOrderByVisitDateTimeAsc();
}
