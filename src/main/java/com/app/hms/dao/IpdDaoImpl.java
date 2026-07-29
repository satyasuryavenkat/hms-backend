package com.app.hms.dao;

import com.app.hms.common.Enums.AdmissionStatus;
import com.app.hms.entity.*;
import com.app.hms.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IpdDaoImpl implements IpdDao {
  private final IpdAdmissionRepository admissions;
  private final IpdChargeRepository charges;
  private final IpdAdvanceRepository advances;

  public IpdAdmission saveAdmission(IpdAdmission a) {
    return admissions.save(a);
  }

  public Optional<IpdAdmission> findAdmission(Long id) {
    return admissions.findById(id);
  }

  public List<IpdAdmission> search(String q, AdmissionStatus s) {
    return admissions.search(q, s);
  }

  public boolean bedOccupied(String b) {
    return admissions.existsByBedNumberAndStatus(b, AdmissionStatus.ACTIVE);
  }

  public IpdCharge saveCharge(IpdCharge c) {
    return charges.save(c);
  }

  public Optional<IpdCharge> findCharge(Long a, Long c) {
    return charges.findByIdAndAdmissionId(c, a);
  }

  public List<IpdCharge> charges(Long id) {
    return charges.findByAdmissionId(id);
  }

  public void deleteCharge(IpdCharge c) {
    charges.delete(c);
  }

  public IpdAdvance saveAdvance(IpdAdvance a) {
    return advances.save(a);
  }

  public List<IpdAdvance> advances(Long id) {
    return advances.findByAdmissionId(id);
  }
}
