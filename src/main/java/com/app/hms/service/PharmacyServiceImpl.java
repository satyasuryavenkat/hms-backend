package com.app.hms.service;

import com.app.hms.common.*;
import com.app.hms.common.Enums.PaymentStatus;
import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;
import com.app.hms.entity.*;
import com.app.hms.mapper.PatientMapper;
import com.app.hms.repository.*;
import java.math.*;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PharmacyServiceImpl implements PharmacyService {
  @Override
  public PageResponse<PharmacySaleResponse> searchSales(String query, int page, int size) {
    var result = sales.search(PageUtils.query(query), PageUtils.request(Math.max(0, page), Math.max(1, size)));
    return new PageResponse<>(result.getContent().stream().map(this::saleResponse).toList(),
        result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
  }
  private static final int MAX_IMAGE_LENGTH = 4_200_000;
  private final MedicineRepository medicines;
  private final PharmacySaleRepository sales;
  private final PatientRepository patients;
  private final PatientMapper patientMapper;
  private final ReferenceNumberGenerator numbers;

  @Override
  public List<MedicineResponse> searchMedicines(String query, Boolean active) {
    return medicines.search(query == null ? "" : query.trim(), active).stream()
        .map(this::medicineResponse)
        .toList();
  }

  @Override
  public MedicineResponse getMedicine(Long id) {
    return medicineResponse(findMedicine(id));
  }

  @Override
  @Transactional
  public MedicineResponse createMedicine(MedicineRequest request) {
    if (clean(request.manufacturerCode()) != null && medicines.existsByManufacturerCodeIgnoreCase(request.manufacturerCode().trim())) {
      throw new BadRequestException("Manufacturer code already exists");
    }
    return medicineResponse(medicines.save(apply(new Medicine(), request)));
  }

  @Override
  @Transactional
  public MedicineResponse updateMedicine(Long id, MedicineRequest request) {
    if (clean(request.manufacturerCode()) != null && medicines.existsByManufacturerCodeIgnoreCaseAndIdNot(
        request.manufacturerCode().trim(), id)) {
      throw new BadRequestException("Manufacturer code already exists");
    }
    return medicineResponse(medicines.save(apply(findMedicine(id), request)));
  }

  @Override
  @Transactional
  public void deleteMedicine(Long id) {
    medicines.delete(findMedicine(id));
  }

  @Override
  @Transactional
  public PharmacySaleResponse createSale(CreatePharmacySaleRequest request) {
    Patient patient = null;
    if (request.patientId() != null) {
      patient =
          patients
              .findById(request.patientId())
              .orElseThrow(() -> new NotFoundException("Patient not found"));
    }
    String customerName = patient != null ? patient.getName() : clean(request.customerName());
    String customerMobile = patient != null ? patient.getMobile() : clean(request.customerMobile());
    if (customerName == null || customerName.isBlank()) {
      throw new BadRequestException("Select a patient or enter a customer name");
    }

    Map<Long, Integer> requestedQuantities = new LinkedHashMap<>();
    request
        .items()
        .forEach(
            item -> requestedQuantities.merge(item.medicineId(), item.quantity(), Math::addExact));

    var sale = new PharmacySale();
    sale.setPatient(patient);
    sale.setCustomerName(customerName);
    sale.setCustomerMobile(customerMobile);
    sale.setDiscount(money(request.discount()));
    sale.setPaymentMode(request.paymentMode());
    sale.setPaymentStatus(PaymentStatus.PAID);

    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal taxAmount = BigDecimal.ZERO;
    for (var entry : requestedQuantities.entrySet()) {
      Medicine medicine =
          medicines
              .findByIdForUpdate(entry.getKey())
              .orElseThrow(() -> new NotFoundException("Medicine not found"));
      int quantity = entry.getValue();
      if (!medicine.isActive()) {
        throw new BadRequestException(medicine.getName() + " is inactive");
      }
      if (medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(LocalDate.now())) {
        throw new BadRequestException(medicine.getName() + " has expired");
      }
      if (medicine.getQuantity() < quantity) {
        throw new BadRequestException(
            "Only " + medicine.getQuantity() + " units of " + medicine.getName() + " are in stock");
      }

      BigDecimal lineSubtotal =
          money(medicine.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
      BigDecimal lineTax =
          money(
              lineSubtotal
                  .multiply(medicine.getTaxPercent())
                  .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
      var item = new PharmacySaleItem();
      item.setSale(sale);
      item.setMedicineId(medicine.getId());
      item.setManufacturerCode(medicine.getManufacturerCode());
      item.setMedicineName(medicine.getName());
      item.setType(medicine.getType());
      item.setBatchNumber(medicine.getBatchNumber());
      item.setExpiryDate(medicine.getExpiryDate());
      item.setQuantity(quantity);
      item.setUnitPrice(medicine.getUnitPrice());
      item.setTaxPercent(medicine.getTaxPercent());
      item.setLineSubtotal(lineSubtotal);
      item.setTaxAmount(lineTax);
      item.setLineTotal(lineSubtotal.add(lineTax));
      sale.getItems().add(item);
      subtotal = subtotal.add(lineSubtotal);
      taxAmount = taxAmount.add(lineTax);
      medicine.setQuantity(medicine.getQuantity() - quantity);
    }

    BigDecimal beforeDiscount = subtotal.add(taxAmount);
    if (sale.getDiscount().compareTo(beforeDiscount) > 0) {
      throw new BadRequestException("Discount cannot exceed the bill amount");
    }
    sale.setSubtotal(money(subtotal));
    sale.setTaxAmount(money(taxAmount));
    sale.setTotalPayable(money(beforeDiscount.subtract(sale.getDiscount())));
    sale.setPaidAmount(sale.getTotalPayable());
    sale = sales.save(sale);
    sale.setInvoiceNumber(numbers.dated("PHM", sale.getId()));
    return saleResponse(sales.save(sale));
  }

  @Override
  public PharmacySaleResponse getSale(Long id) {
    return saleResponse(
        sales.findById(id).orElseThrow(() -> new NotFoundException("Pharmacy invoice not found")));
  }

  private Medicine findMedicine(Long id) {
    return medicines.findById(id).orElseThrow(() -> new NotFoundException("Medicine not found"));
  }

  private Medicine apply(Medicine medicine, MedicineRequest request) {
    String image = clean(request.imageData());
    if (image != null && image.length() > MAX_IMAGE_LENGTH) {
      throw new BadRequestException("Medicine image must be smaller than 3 MB");
    }
    if (clean(request.manufacturerCode()) != null) {
      medicine.setManufacturerCode(request.manufacturerCode().trim());
    } else if (medicine.getManufacturerCode() == null) {
      medicine.setManufacturerCode("MED-" + UUID.randomUUID());
    }
    medicine.setName(request.name().trim());
    if (request.genericName() != null) medicine.setGenericName(clean(request.genericName()));
    medicine.setType(request.type().trim());
    if (request.manufacturerName() != null) medicine.setManufacturerName(clean(request.manufacturerName()));
    if (request.batchNumber() != null) medicine.setBatchNumber(clean(request.batchNumber()));
    if (request.description() != null) medicine.setDescription(clean(request.description()));
    if (request.imageData() != null) medicine.setImageData(image);
    if (request.quantity() != null) medicine.setQuantity(request.quantity());
    medicine.setUnitPrice(money(request.unitPrice()));
    if (request.taxPercent() != null) medicine.setTaxPercent(money(request.taxPercent()));
    if (request.expiryDate() != null) medicine.setExpiryDate(request.expiryDate());
    medicine.setActive(request.active());
    return medicine;
  }

  private MedicineResponse medicineResponse(Medicine m) {
    return new MedicineResponse(
        m.getId(),
        m.getManufacturerCode(),
        m.getName(),
        m.getGenericName(),
        m.getType(),
        m.getManufacturerName(),
        m.getBatchNumber(),
        m.getDescription(),
        m.getImageData(),
        m.getQuantity(),
        m.getUnitPrice(),
        m.getTaxPercent(),
        m.getExpiryDate(),
        m.isActive(),
        m.getExpiryDate() != null && m.getExpiryDate().isBefore(LocalDate.now()),
        m.getCreatedAt(),
        m.getUpdatedAt());
  }

  private PharmacySaleResponse saleResponse(PharmacySale sale) {
    List<PharmacySaleResponse.Item> items =
        sale.getItems().stream()
            .map(
                i ->
                    new PharmacySaleResponse.Item(
                        i.getMedicineId(),
                        i.getManufacturerCode(),
                        i.getMedicineName(),
                        i.getType(),
                        i.getBatchNumber(),
                        i.getExpiryDate(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getTaxPercent(),
                        i.getLineSubtotal(),
                        i.getTaxAmount(),
                        i.getLineTotal()))
            .toList();
    return new PharmacySaleResponse(
        sale.getId(),
        sale.getInvoiceNumber(),
        sale.getPatient() == null ? null : patientMapper.toResponse(sale.getPatient()),
        sale.getCustomerName(),
        sale.getCustomerMobile(),
        items,
        sale.getSubtotal(),
        sale.getTaxAmount(),
        sale.getDiscount(),
        sale.getTotalPayable(),
        sale.getPaidAmount(),
        sale.getPaymentMode(),
        sale.getPaymentStatus(),
        sale.getCreatedAt());
  }

  private static String clean(String value) {
    return value == null || value.trim().isEmpty() ? null : value.trim();
  }

  private static BigDecimal money(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP);
  }
}
