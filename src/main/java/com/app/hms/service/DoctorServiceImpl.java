package com.app.hms.service;

import com.app.hms.common.BadRequestException;
import com.app.hms.common.NotFoundException;
import com.app.hms.dao.DoctorDao;
import com.app.hms.dto.request.DoctorRequest;
import com.app.hms.dto.response.DoctorResponse;
import com.app.hms.entity.Doctor;
import com.app.hms.mapper.DoctorMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorServiceImpl implements DoctorService {
  private final DoctorDao dao;
  private final DoctorMapper mapper;

  @Override
  public List<DoctorResponse> findAll(Boolean active, String department) {
    return dao.findAll(active, department).stream().map(mapper::toResponse).toList();
  }

  @Override
  public DoctorResponse findResponseById(Long id) {
    return mapper.toResponse(findEntityById(id));
  }

  @Override
  public Doctor findEntityById(Long id) {
    return dao.findById(id).orElseThrow(() -> new NotFoundException("Doctor not found"));
  }

  @Override
  @Transactional
  public DoctorResponse create(DoctorRequest request) {
    if (dao.existsByCode(request.doctorCode(), null)) {
      throw new BadRequestException("Doctor code already exists");
    }
    return mapper.toResponse(dao.save(updateEntity(new Doctor(), request)));
  }

  @Override
  @Transactional
  public DoctorResponse update(Long id, DoctorRequest request) {
    if (dao.existsByCode(request.doctorCode(), id)) {
      throw new BadRequestException("Doctor code already exists");
    }
    return mapper.toResponse(dao.save(updateEntity(findEntityById(id), request)));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    Doctor doctor = findEntityById(id);
    try {
      dao.delete(doctor);
    } catch (DataIntegrityViolationException exception) {
      throw new BadRequestException(
          "Doctor cannot be deleted because hospital records use this doctor. Mark the doctor inactive instead.");
    }
  }

  private Doctor updateEntity(Doctor doctor, DoctorRequest request) {
    doctor.setDoctorCode(request.doctorCode().trim());
    doctor.setName(request.name().trim());
    doctor.setDepartment(request.department().trim());
    doctor.setSpecialization(request.specialization());
    doctor.setConsultationFee(request.consultationFee());
    doctor.setActive(request.active());
    return doctor;
  }
}
