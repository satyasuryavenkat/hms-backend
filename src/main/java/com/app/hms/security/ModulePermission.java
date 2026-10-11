package com.app.hms.security;

import com.app.hms.common.Enums.UserRole;
import java.util.*;

public final class ModulePermission {
  public static final String OP = "MODULE_OP";
  public static final String LAB = "MODULE_LAB";
  public static final String IPD = "MODULE_IPD";
  public static final String DAYCARE = "MODULE_DAYCARE";
  public static final String PHARMACY = "MODULE_PHARMACY";
  public static final String FOLLOWUPS = "MODULE_FOLLOWUPS";
  public static final String NONE = "MODULE_NONE";

  private ModulePermission() {}

  public static Set<String> effective(UserRole role, Set<String> permissions) {
    Set<String> assigned = permissions == null ? Set.of() : permissions;
    if (role == UserRole.ADMINISTRATOR) {
      return Set.of(OP, LAB, IPD, DAYCARE, PHARMACY, FOLLOWUPS);
    }
    Set<String> modules = new LinkedHashSet<>();
    assigned.stream().filter(ModulePermission::isAccessModule).forEach(modules::add);
    if (!modules.isEmpty() || assigned.contains(NONE)) return modules;
    return defaults(role);
  }

  public static boolean isAccessModule(String permission) {
    return Set.of(OP, LAB, IPD, DAYCARE, PHARMACY, FOLLOWUPS).contains(permission);
  }

  private static Set<String> defaults(UserRole role) {
    return switch (role) {
      case RECEPTIONIST -> Set.of(OP, FOLLOWUPS);
      case LAB_TECHNICIAN -> Set.of(LAB);
      case BILLING_EXECUTIVE -> Set.of(OP, LAB, IPD, DAYCARE, PHARMACY);
      case DOCTOR -> Set.of(OP, LAB, IPD, DAYCARE, FOLLOWUPS);
      case ADMINISTRATOR -> Set.of(OP, LAB, IPD, DAYCARE, PHARMACY, FOLLOWUPS);
    };
  }
}
