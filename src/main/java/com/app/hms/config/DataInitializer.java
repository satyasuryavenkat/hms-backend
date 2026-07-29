package com.app.hms.config;

import com.app.hms.common.Enums.UserRole;
import com.app.hms.dao.DoctorDao;
import com.app.hms.dao.UserDao;
import com.app.hms.entity.AppUser;
import com.app.hms.entity.Doctor;
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

  @Value("${app.bootstrap.enabled:false}")
  private boolean bootstrapEnabled;

  @Value("${app.bootstrap.admin-username:admin@medora.in}")
  private String adminUsername;

  @Value("${app.bootstrap.admin-password:}")
  private String adminPassword;

  @Bean
  CommandLineRunner seed() {
    return args -> {
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
