package com.app.hms.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.app.hms.common.BadRequestException;

@Service
@RequiredArgsConstructor
public class OverviewService {
  private final JdbcTemplate jdbc;
  private static final ZoneId HOSPITAL_ZONE = ZoneId.of("Asia/Kolkata");

  public record Metric(String id, long count, long records, BigDecimal billed, BigDecimal collected) {}
  public record BedPatient(long admissionId, String admissionNumber, String careType,
      String status, String ward, String bedNumber, java.time.LocalDateTime admissionDateTime,
      String patientName, String uhid, Integer age, String gender, String mobile,
      String consultantName, String diagnosis, String attendantName, String attendantMobile) {}

  @Transactional(readOnly = true)
  public List<BedPatient> currentBeds() {
    return jdbc.query("""
        SELECT a.id, a.ipd_number, COALESCE(a.care_type,'IPD') care_type,
          a.status, a.ward, a.bed_number, a.admission_date_time,
          p.name patient_name, p.uhid, p.age, p.gender, p.mobile,
          d.name consultant_name, a.provisional_diagnosis, a.attendant_name, a.attendant_mobile
        FROM ipd_admission a
        JOIN patients p ON p.id = a.patient_id
        JOIN doctors d ON d.id = a.consultant_id
        WHERE a.status IN ('ACTIVE','DISCHARGE_PLANNED')
          AND a.bed_number IS NOT NULL AND TRIM(a.bed_number) <> ''
        ORDER BY COALESCE(a.care_type,'IPD'), a.ward, a.bed_number, p.name, a.id
        """, (rs, row) -> new BedPatient(rs.getLong("id"), rs.getString("ipd_number"),
          rs.getString("care_type"), rs.getString("status"), rs.getString("ward"),
          rs.getString("bed_number"), rs.getObject("admission_date_time", java.time.LocalDateTime.class),
          rs.getString("patient_name"), rs.getString("uhid"), rs.getObject("age", Integer.class),
          rs.getString("gender"), rs.getString("mobile"), rs.getString("consultant_name"),
          rs.getString("provisional_diagnosis"), rs.getString("attendant_name"), rs.getString("attendant_mobile")));
  }
  public record Overview(LocalDate startDate, LocalDate endDate, long newPatients,
      long patientsServed, List<Metric> departments, BigDecimal billed, BigDecimal collected) {}

  @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
  public Overview get(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null || startDate.isAfter(endDate)
        || startDate.getYear() < 1900 || endDate.getYear() > 9998) {
      throw new BadRequestException("Provide a valid start and end date");
    }
    var start = startDate.atStartOfDay(HOSPITAL_ZONE).toOffsetDateTime();
    var end = endDate.plusDays(1).atStartOfDay(HOSPITAL_ZONE).toOffsetDateTime();
    var localStart = startDate.atStartOfDay();
    var localEnd = endDate.plusDays(1).atStartOfDay();
    Metric op = jdbc.queryForObject("""
        SELECT COUNT(*) n, COALESCE(SUM(total_payable),0) billed,
        COALESCE(SUM(paid_amount),0) collected FROM op_visit
        WHERE created_at >= ? AND created_at < ?
        """, (rs, row) -> new Metric("op", rs.getLong("n"), rs.getLong("n"),
            rs.getBigDecimal("billed"), rs.getBigDecimal("collected")), start, end);
    Metric lab = jdbc.queryForObject("""
        SELECT COUNT(*) n, COALESCE(SUM(t.n),0) tests,
        COALESCE(SUM(o.total_payable),0) billed, COALESCE(SUM(o.paid_amount),0) collected
        FROM lab_order o LEFT JOIN
        (SELECT order_id, COUNT(*) n FROM lab_order_item GROUP BY order_id) t ON t.order_id=o.id
        WHERE o.created_at >= ? AND o.created_at < ?
        AND (o.report_status IS NULL OR o.report_status <> 'CANCELLED')
        """, (rs, row) -> new Metric("lab", rs.getLong("tests"), rs.getLong("n"),
            rs.getBigDecimal("billed"), rs.getBigDecimal("collected")), start, end);
    var departments = List.of(op, admission("ipd", "IPD", localStart, localEnd),
        admission("daycare", "DAYCARE", localStart, localEnd), lab);
    long newPatients = jdbc.queryForObject(
        "SELECT COUNT(*) FROM patients WHERE created_at >= ? AND created_at < ?",
        Long.class, start, end);
    long patientsServed = jdbc.queryForObject("""
        SELECT COUNT(*) FROM (
          SELECT patient_id FROM op_visit WHERE created_at >= ? AND created_at < ?
          UNION
          SELECT patient_id FROM lab_order WHERE created_at >= ? AND created_at < ?
            AND (report_status IS NULL OR report_status <> 'CANCELLED')
          UNION
          SELECT patient_id FROM ipd_admission WHERE admission_date_time >= ? AND admission_date_time < ?
        ) served
        """, Long.class, start, end, start, end, localStart, localEnd);
    return new Overview(startDate, endDate, newPatients, patientsServed, departments,
        departments.stream().map(Metric::billed).reduce(BigDecimal.ZERO, BigDecimal::add),
        departments.stream().map(Metric::collected).reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  private Metric admission(String id, String careType, Object start, Object end) {
    return jdbc.queryForObject("""
        SELECT COUNT(*) n,
          COALESCE(SUM(GREATEST(COALESCE(c.amount,0)-COALESCE(a.discount,0),0)),0) billed,
          COALESCE(SUM(COALESCE(p.amount,0)+COALESCE(a.final_paid_amount,0)),0) collected
        FROM ipd_admission a
        LEFT JOIN (SELECT admission_id,SUM(amount) amount FROM ipd_charge GROUP BY admission_id) c
          ON c.admission_id=a.id
        LEFT JOIN (SELECT admission_id,SUM(amount) amount FROM ipd_advance GROUP BY admission_id) p
          ON p.admission_id=a.id
        WHERE COALESCE(a.care_type,'IPD')=? AND a.admission_date_time >= ? AND a.admission_date_time < ?
        """, (rs, row) -> new Metric(id, rs.getLong("n"), rs.getLong("n"),
            rs.getBigDecimal("billed"), rs.getBigDecimal("collected")), careType, start, end);
  }
}
