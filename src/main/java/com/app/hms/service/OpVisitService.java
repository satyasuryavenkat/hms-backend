package com.app.hms.service;

import com.app.hms.dto.request.OpVisitRequest;
import com.app.hms.dto.response.*;
import java.time.LocalDate;

public interface OpVisitService {
  OpVisitResponse create(OpVisitRequest request);

  OpVisitResponse update(Long id, OpVisitRequest request);

  OpVisitResponse findById(Long id);

  OpVisitPageResponse search(String query, LocalDate date, int page, int size);

  byte[] generateReceiptPdf(Long id);
}
