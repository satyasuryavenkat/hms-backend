package com.app.hms.service;

import com.app.hms.common.*;
import com.app.hms.common.Enums.LabReportStatus;
import com.app.hms.dao.LabTestDao;
import com.app.hms.dto.request.LabTestRequest;
import com.app.hms.dto.response.LabTestResponse;
import com.app.hms.entity.LabTest;
import com.app.hms.mapper.LabTestMapper;
import com.app.hms.repository.LabOrderItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabTestServiceImpl implements LabTestService {
  private final LabTestDao dao;
  private final LabTestMapper mapper;
  private final LabOrderItemRepository orderItems;

  public List<LabTestResponse> search(String q, String d, Boolean a) {
    return dao.search(q == null ? "" : q, d, a).stream().map(mapper::toResponse).toList();
  }

  public LabTestResponse findById(Long id) {
    return mapper.toResponse(entity(id));
  }

  @Transactional
  public LabTestResponse create(LabTestRequest r) {
    if (dao.existsByCode(r.code())) throw new BadRequestException("Lab test code already exists");
    LabTest t = new LabTest();
    mapper.update(t, r);
    return mapper.toResponse(dao.save(t));
  }

  @Transactional
  public LabTestResponse update(Long id, LabTestRequest r) {
    if (dao.existsByCode(r.code(), id)) {
      throw new BadRequestException("Lab test code already exists");
    }
    LabTest test = entity(id);
    mapper.update(test, r);
    return mapper.toResponse(dao.save(test));
  }

  @Transactional
  public LabTestResponse updateReportTemplate(Long id, String html) {
    if (html == null || html.isBlank()) {
      throw new BadRequestException("Report template cannot be empty");
    }
    if (html.length() > 500_000) {
      throw new BadRequestException("Report template is too large");
    }
    String normalized = html.toLowerCase(java.util.Locale.ROOT);
    if (normalized.contains("<script")
        || normalized.contains("javascript:")
        || normalized.matches("(?s).*(onerror|onload|onclick)\\s*=.*")) {
      throw new BadRequestException("Report template contains unsafe HTML");
    }
    LabTest test = entity(id);
    test.setReportTemplateHtml(html);
    LabTest saved = dao.save(test);
    orderItems.updateReportTemplateForUnfinishedOrders(
        id, html, List.of(LabReportStatus.PENDING, LabReportStatus.DRAFT));
    return mapper.toResponse(saved);
  }

  @Transactional
  public void delete(Long id) {
    LabTest test = entity(id);
    try {
      dao.delete(test);
    } catch (DataIntegrityViolationException exception) {
      throw new BadRequestException(
          "Lab test cannot be deleted because hospital records use it. Mark the test inactive instead.");
    }
  }

  private LabTest entity(Long id) {
    return dao.findById(id).orElseThrow(() -> new NotFoundException("Lab test not found"));
  }
}
