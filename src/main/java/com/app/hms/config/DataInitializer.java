package com.app.hms.config;

import com.app.hms.common.Enums.UserRole;
import com.app.hms.dao.DoctorDao;
import com.app.hms.dao.UserDao;
import com.app.hms.entity.AppUser;
import com.app.hms.entity.Doctor;
import com.app.hms.entity.IpdChargeCatalogItem;
import com.app.hms.repository.IpdChargeCatalogRepository;
import java.math.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
  private final UserDao users;
  private final DoctorDao doctors;
  private final PasswordEncoder encoder;
  private final IpdChargeCatalogRepository ipdChargeCatalog;

  @Value("${app.bootstrap.enabled:false}")
  private boolean bootstrapEnabled;

  @Value("${app.bootstrap.admin-username:admin@medora.in}")
  private String adminUsername;

  @Value("${app.bootstrap.admin-password:}")
  private String adminPassword;

  @Bean
  CommandLineRunner seed() {
    return args -> {
      seedIpdChargeCatalog();
      if (!bootstrapEnabled) {
        return;
      }
      if (adminPassword == null || adminPassword.isBlank()) {
        throw new IllegalStateException(
            "BOOTSTRAP_ADMIN_PASSWORD must be set when BOOTSTRAP_DATA is enabled");
      }
      if (users.count() == 0) {
        var u = new AppUser();
        u.setUsername(adminUsername);
        u.setPassword(encoder.encode(adminPassword));
        u.setDisplayName("Admin");
        u.setRole(UserRole.ADMINISTRATOR);
        u.setPermissions(
            Set.of(
                "OP_REGISTER",
                "OP_BILL",
                "LAB_BILL",
                "LAB_REPORT",
                "IPD_REGISTER",
                "IPD_BILL",
                "IPD_DISCHARGE"));
        users.save(u);
      }
      if (doctors.count() == 0) {
        saveDoctor("DOC-001", "Dr. Priya Nair", "General Medicine", "Internal Medicine", 700);
        saveDoctor("DOC-002", "Dr. Raj Malhotra", "Cardiology", "Cardiologist", 1000);
      }
    };
  }

  private void seedIpdChargeCatalog() {
    if (ipdChargeCatalog.count() > 0) return;
    saveIpdCharge("ROOM_CHARGES", "Room Charges", "ROOM-GENERAL", "General ward stay", "Inpatient Services", 1000);
    saveIpdCharge("ROOM_CHARGES", "Room Charges", "ROOM-PRIVATE", "Private room stay", "Inpatient Services", 2200);
    saveIpdCharge("ROOM_CHARGES", "Room Charges", "ROOM-DELUXE", "Deluxe room stay", "Inpatient Services", 3200);
    saveIpdCharge("NURSING", "Nursing Charges", "NURSING-DAY", "Nursing charge per day", "Nursing", 500);
    saveIpdCharge("PROCEDURES", "Procedures", "PROCEDURE-GENERAL", "General procedure charge", "Inpatient Services", 0);
  }

  private void saveIpdCharge(String category, String categoryLabel, String code, String name, String department, int rate) {
    var item = new IpdChargeCatalogItem();
    item.setCategory(category);
    item.setCategoryLabel(categoryLabel);
    item.setServiceCode(code);
    item.setServiceName(name);
    item.setDepartment(department);
    item.setDefaultRate(BigDecimal.valueOf(rate));
    item.setActive(true);
    ipdChargeCatalog.save(item);
  }

  private void saveDoctor(String code, String name, String dept, String spec, int fee) {
    var d = new Doctor();
    d.setDoctorCode(code);
    d.setName(name);
    d.setDepartment(dept);
    d.setSpecialization(spec);
    d.setConsultationFee(BigDecimal.valueOf(fee));
    d.setActive(true);
    doctors.save(d);
  }
}
