package com.app.hms.service;

import com.app.hms.common.BadRequestException;
import com.app.hms.dto.response.*;
import jakarta.persistence.EntityManager;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingRecordsService {
  private final EntityManager em;
  private final OpVisitService op;
  private final LabOrderService lab;
  private final IpdService ipd;
  private final PharmacyService pharmacy;
  public record Detail(Object record, FinalBillResponse bill) {}

  @Transactional(readOnly = true)
  public PageResponse<Detail> records(String section, String query, LocalDate startDate,
      LocalDate endDate, int page, int size, boolean overview) {
    if ((startDate == null) != (endDate == null) || (startDate != null &&
        (startDate.isAfter(endDate) || startDate.getYear() < 1900 || endDate.getYear() > 9998)))
      throw new BadRequestException("Select a valid start and end date");
    if (overview && startDate == null) throw new BadRequestException("Select both dates");
    boolean admission = section.equals("ipd") || section.equals("daycare");
    String entity = switch (section) {
      case "op" -> "OpVisit";
      case "lab" -> "LabOrder";
      case "ipd", "daycare" -> "IpdAdmission";
      case "pharmacy" -> "PharmacySale";
      default -> throw new BadRequestException("Unknown billing section");
    };
    String number = admission ? "r.ipdNumber" : section.equals("op") ? "r.opNumber" : section.equals("lab") ? "r.orderNumber" : "r.invoiceNumber";
    String date = admission ? (overview ? "r.admissionDateTime" : "r.settledAt") : "r.createdAt";
    String from = " from " + entity + " r left join r.patient p where 1=1";
    if (admission) from += section.equals("ipd") ? " and (r.careType=IPD or r.careType is null)" : " and r.careType=DAYCARE";
    if (!overview) from += admission ? " and r.settledAt is not null" : " and r.paymentStatus=PAID";
    if (section.equals("lab")) from += " and (r.reportStatus is null or r.reportStatus<>CANCELLED)";
    from += " and (lower(p.name) like :q or lower(p.uhid) like :q or p.mobile like :q or lower(" + number + ") like :q";
    if (section.equals("op")) from += " or lower(r.receiptNumber) like :q";
    if (section.equals("pharmacy")) from += " or lower(r.customerName) like :q or r.customerMobile like :q";
    from += ")";
    if (startDate != null) from += " and " + date + ">=:start and " + date + "<:end";
    var count = em.createQuery("select count(r)" + from, Long.class);
    var ids = em.createQuery("select r.id" + from + " order by " + date + " desc,r.id desc", Long.class);
    for (var statement : List.of(count, ids)) {
      statement.setParameter("q", "%" + Objects.requireNonNullElse(query, "").trim().toLowerCase(Locale.ROOT) + "%");
      if (startDate != null) {
        statement.setParameter("start", admission && overview ? startDate.atStartOfDay() : startDate.atStartOfDay(ZoneId.of("Asia/Kolkata")).toOffsetDateTime());
        statement.setParameter("end", admission && overview ? endDate.plusDays(1).atStartOfDay() : endDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).toOffsetDateTime());
      }
    }
    int safePage = Math.max(0, page), safeSize = Math.max(1, Math.min(100, size));
    long total = count.getSingleResult();
    var content = ids.setFirstResult(Math.multiplyExact(safePage, safeSize)).setMaxResults(safeSize).getResultList().stream().map(id -> new Detail(switch (section) {
      case "op" -> op.findById(id);
      case "lab" -> lab.findById(id);
      case "pharmacy" -> pharmacy.getSale(id);
      case "daycare" -> ipd.findDaycareById(id);
      default -> ipd.findById(id);
    }, admission ? ipd.finalBill(id) : null)).toList();
    return new PageResponse<>(content, safePage, safeSize, total, (int) Math.ceil((double) total / safeSize));
  }
}
