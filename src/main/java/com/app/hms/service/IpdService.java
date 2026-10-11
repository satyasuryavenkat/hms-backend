package com.app.hms.service;

import com.app.hms.common.Enums.AdmissionStatus;
import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import java.util.List;

public interface IpdService {
  IpdAdmissionResponse updateDates(Long id, AdmissionDatesRequest request);
  IpdAdmissionResponse create(IpdAdmissionRequest request);

  IpdAdmissionResponse createDaycare(IpdAdmissionRequest request);

  IpdAdmissionResponse update(Long id, IpdAdmissionRequest request);

  IpdAdmissionResponse updateDaycare(Long id, IpdAdmissionRequest request);

  List<IpdAdmissionResponse> search(String query, AdmissionStatus status);

  List<IpdAdmissionResponse> searchDaycare(String query, AdmissionStatus status);

  IpdAdmissionResponse findById(Long id);

  IpdAdmissionResponse findDaycareById(Long id);

  IpdChargesResponse charges(Long id);

  IpdChargeResponse addCharge(Long id, IpdChargeRequest request);

  IpdChargeResponse updateCharge(Long id, Long chargeId, IpdChargeRequest request);

  void deleteCharge(Long id, Long chargeId);

  List<?> catalog();

  List<IpdChargeCatalogItemResponse> catalogItems(Boolean active);

  IpdChargeCatalogItemResponse createCatalogItem(IpdChargeCatalogRequest request);

  IpdChargeCatalogItemResponse updateCatalogItem(Long id, IpdChargeCatalogRequest request);

  void deleteCatalogItem(Long id);

  IpdAdvanceResponse addAdvance(Long id, IpdAdvanceRequest request);

  List<IpdAdvanceResponse> advances(Long id);

  FinalBillResponse finalBill(Long id);

  FinalBillResponse settle(Long id, FinalBillSettlementRequest request);

  IpdAdmissionResponse saveDischarge(Long id, DischargeRequest request);

  IpdAdmissionResponse finalizeDischarge(Long id, FinalizeDischargeRequest request);

  byte[] finalBillPdf(Long id);

  byte[] dischargePdf(Long id);
}
