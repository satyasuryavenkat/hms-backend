package com.app.hms.common;

public final class Enums {
  private Enums() {}

  public enum Gender {
    MALE,
    FEMALE,
    OTHER
  }

  public enum PaymentMode {
    CASH,
    UPI,
    CARD,
    INSURANCE
  }

  public enum PaymentStatus {
    PENDING,
    PARTIALLY_PAID,
    PAID,
    REFUNDED
  }

  public enum VisitType {
    NEW_CONSULTATION,
    FOLLOW_UP
  }

  public enum FollowUpStatus {
    PENDING,
    REMINDED
  }

  public enum LabPriority {
    ROUTINE,
    URGENT
  }

  public enum LabReportStatus {
    PENDING,
    DRAFT,
    VERIFIED,
    PUBLISHED,
    CANCELLED
  }

  public enum LabSignatureDesignation {
    LAB_INCHARGE,
    CONSULTANT_PATHOLOGIST
  }

  public enum LabParameterType {
    NUMERIC,
    TEXT,
    HEADING
  }

  public enum SpecimenType {
    BLOOD,
    SERUM,
    URINE,
    STOOL,
    SWAB,
    SPUTUM,
    PUS,
    SLIDE_PREPARATION,
    OTHER
  }

  public enum AdmissionType {
    PLANNED,
    EMERGENCY,
    TRANSFER
  }

  public enum CareType {
    IPD,
    DAYCARE
  }

  public enum AdmissionStatus {
    ACTIVE,
    DISCHARGE_PLANNED,
    DISCHARGED,
    CANCELLED
  }

  public enum DischargeType {
    ROUTINE,
    LAMA,
    TRANSFER,
    DEATH
  }

  public enum UserRole {
    ADMINISTRATOR,
    RECEPTIONIST,
    LAB_TECHNICIAN,
    BILLING_EXECUTIVE,
    DOCTOR
  }
}
