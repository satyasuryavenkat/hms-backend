package com.app.hms.service;

import com.app.hms.dto.request.LabTestRequest;
import com.app.hms.dto.response.LabTestResponse;
import java.util.List;

public interface LabTestService {
  List<LabTestResponse> search(String query, String department, Boolean active);

  LabTestResponse findById(Long id);

  LabTestResponse create(LabTestRequest request);

  LabTestResponse update(Long id, LabTestRequest request);

  LabTestResponse updateReportTemplate(Long id, String html);

  void delete(Long id);
}
