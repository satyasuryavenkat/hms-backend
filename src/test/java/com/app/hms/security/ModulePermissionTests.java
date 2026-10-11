package com.app.hms.security;

import static org.junit.jupiter.api.Assertions.*;

import com.app.hms.common.Enums.UserRole;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ModulePermissionTests {
  @Test
  void appliesLegacyRoleDefaultsUntilModulesAreExplicitlyAllocated() {
    assertEquals(
        Set.of(ModulePermission.OP, ModulePermission.FOLLOWUPS),
        ModulePermission.effective(UserRole.RECEPTIONIST, Set.of()));
    assertEquals(
        Set.of(ModulePermission.LAB),
        ModulePermission.effective(UserRole.LAB_TECHNICIAN, Set.of("LAB_REPORT")));
  }

  @Test
  void explicitAllocationOverridesRoleDefaultsAndSupportsNoAccess() {
    assertEquals(
        Set.of(ModulePermission.PHARMACY),
        ModulePermission.effective(UserRole.RECEPTIONIST, Set.of(ModulePermission.PHARMACY)));
    assertTrue(
        ModulePermission.effective(UserRole.DOCTOR, Set.of(ModulePermission.NONE)).isEmpty());
  }

  @Test
  void administratorAlwaysReceivesEveryModule() {
    assertEquals(
        Set.of(
            ModulePermission.OP,
            ModulePermission.LAB,
            ModulePermission.IPD,
            ModulePermission.DAYCARE,
            ModulePermission.PHARMACY,
            ModulePermission.FOLLOWUPS),
        ModulePermission.effective(UserRole.ADMINISTRATOR, Set.of(ModulePermission.NONE)));
  }
}
