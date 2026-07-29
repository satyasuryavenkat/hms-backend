package com.app.hms.dao;

import com.app.hms.common.Enums.AdmissionStatus;
import com.app.hms.entity.*;
import java.util.*;

public interface IpdDao {
  IpdAdmission saveAdmission(IpdAdmission a);

  Optional<IpdAdmission> findAdmission(Long id);

  List<IpdAdmission> search(String q, AdmissionStatus status);

  boolean bedOccupied(String bed);

  IpdCharge saveCharge(IpdCharge c);

  Optional<IpdCharge> findCharge(Long admissionId, Long chargeId);

  List<IpdCharge> charges(Long admissionId);

  void deleteCharge(IpdCharge c);

  IpdAdvance saveAdvance(IpdAdvance a);

  List<IpdAdvance> advances(Long admissionId);
}
