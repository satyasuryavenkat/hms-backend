package com.app.hms.repository;

import com.app.hms.common.Enums.LabReportStatus;
import com.app.hms.entity.LabOrderItem;
import java.util.Collection;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LabOrderItemRepository extends JpaRepository<LabOrderItem, Long> {
  @Modifying(flushAutomatically = true)
  @Query(
      "update LabOrderItem item set item.reportTemplateHtml=:html "
          + "where item.testId=:testId and item.order.reportStatus in :statuses")
  int updateReportTemplateForUnfinishedOrders(
      @Param("testId") Long testId,
      @Param("html") String html,
      @Param("statuses") Collection<LabReportStatus> statuses);
}
