package com.app.hms.config;

import java.sql.Connection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSchemaMigration {
  private final JdbcTemplate jdbc;

  @EventListener(ApplicationReadyEvent.class)
  public void migrateLegacySchema() throws Exception {
    try (Connection connection = jdbc.getDataSource().getConnection()) {
      if (!"PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName())) return;
    }

    jdbc.execute("ALTER TABLE pharmacy_medicines ALTER COLUMN expiry_date DROP NOT NULL");

    jdbc.execute("ALTER TABLE patients ADD COLUMN IF NOT EXISTS age integer");
    if (columnExists("patients", "date_of_birth")) {
      jdbc.execute(
          "UPDATE patients SET age = GREATEST(0, LEAST(130, "
              + "DATE_PART('year', AGE(CURRENT_DATE, date_of_birth))::integer)) "
              + "WHERE age IS NULL AND date_of_birth IS NOT NULL");
      jdbc.execute("ALTER TABLE patients DROP COLUMN date_of_birth");
    }
    jdbc.execute("UPDATE patients SET age = 0 WHERE age IS NULL");
    jdbc.execute("ALTER TABLE patients ALTER COLUMN age SET NOT NULL");

    // Hibernate schema updates do not expand existing enum CHECK constraints.
    // Replace each generated constraint atomically to support all sample types on existing installations.
    for (String table : List.of("lab_test", "lab_order_item", "lab_specimens")) {
      jdbc.execute(
          "ALTER TABLE " + table
              + " DROP CONSTRAINT IF EXISTS " + table + "_specimen_type_check,"
              + " ADD CONSTRAINT " + table + "_specimen_type_check"
              + " CHECK (specimen_type IN ('BLOOD', 'SERUM', 'URINE', 'STOOL', 'SWAB', 'SPUTUM', 'PUS', 'SLIDE_PREPARATION', 'OTHER'))");
    }

    jdbc.execute("ALTER TABLE ipd_admission ADD COLUMN IF NOT EXISTS care_type varchar(255)");
    jdbc.execute("UPDATE ipd_admission SET care_type = 'IPD' WHERE care_type IS NULL");

    if (columnExists("lab_result", "remarks")) {
      jdbc.execute("ALTER TABLE lab_result DROP COLUMN remarks");
    }

    // A bed is unique only while an admission is active. Older installations created a
    // database-wide unique constraint, which prevented a discharged bed from being reused.
    List<String> bedConstraints =
        jdbc.queryForList(
            "SELECT tc.constraint_name FROM information_schema.table_constraints tc "
                + "JOIN information_schema.key_column_usage kcu "
                + "ON tc.constraint_schema = kcu.constraint_schema "
                + "AND tc.constraint_name = kcu.constraint_name "
                + "AND tc.table_name = kcu.table_name "
                + "WHERE tc.table_schema = current_schema() "
                + "AND tc.table_name = 'ipd_admission' "
                + "AND tc.constraint_type = 'UNIQUE' "
                + "GROUP BY tc.constraint_name "
                + "HAVING COUNT(*) = 1 AND MAX(kcu.column_name) = 'bed_number'",
            String.class);
    for (String constraint : bedConstraints) {
      jdbc.execute(
          "ALTER TABLE ipd_admission DROP CONSTRAINT \""
              + constraint.replace("\"", "\"\"")
              + "\"");
    }
    jdbc.execute("DROP INDEX IF EXISTS uq_ipd_admission_occupied_bed");
    jdbc.execute(
        "CREATE UNIQUE INDEX uq_ipd_admission_occupied_bed "
            + "ON ipd_admission (care_type, bed_number) "
            + "WHERE status IN ('ACTIVE', 'DISCHARGE_PLANNED')");
  }

  private boolean columnExists(String table, String column) {
    Integer count =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?",
            Integer.class,
            table,
            column);
    return count != null && count > 0;
  }
}
