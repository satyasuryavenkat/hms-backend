package com.app.hms.service;

import com.app.hms.common.*;
import com.app.hms.dao.LabTestDao;
import com.app.hms.dto.request.LabTestRequest;
import com.app.hms.dto.response.LabTestResponse;
import com.app.hms.entity.LabTest;
import com.app.hms.mapper.LabTestMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabTestServiceImpl implements LabTestService {
  private final LabTestDao dao;
  private final LabTestMapper mapper;

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

  private LabTest entity(Long id) {
    return dao.findById(id).orElseThrow(() -> new NotFoundException("Lab test not found"));
  }
}
