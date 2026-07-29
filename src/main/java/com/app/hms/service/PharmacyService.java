package com.app.hms.service;

import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import java.util.List;

public interface PharmacyService {
  List<MedicineResponse> searchMedicines(String query, Boolean active);

  MedicineResponse getMedicine(Long id);

  MedicineResponse createMedicine(MedicineRequest request);

  MedicineResponse updateMedicine(Long id, MedicineRequest request);

  void deleteMedicine(Long id);

  PharmacySaleResponse createSale(CreatePharmacySaleRequest request);

  PharmacySaleResponse getSale(Long id);
}
