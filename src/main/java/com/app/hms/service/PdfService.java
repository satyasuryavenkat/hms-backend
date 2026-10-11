package com.app.hms.service;

import com.app.hms.common.Enums.LabParameterType;
import com.app.hms.common.Enums.LabSignatureDesignation;
import com.app.hms.entity.LabOrder;
import com.app.hms.entity.LabResult;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class PdfService {
  public byte[] create(String title, Map<String, ?> fields) {
    try (var out = new ByteArrayOutputStream()) {
      var doc = new Document();
      PdfWriter.getInstance(doc, out);
      doc.open();
      doc.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
      doc.add(new Paragraph(" "));
      fields.forEach(
          (k, v) -> {
            try {
              doc.add(new Paragraph(k + ": " + Objects.toString(v, "")));
            } catch (DocumentException e) {
              throw new RuntimeException(e);
            }
          });
      doc.close();
      return out.toByteArray();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to generate PDF", e);
    }
  }

  public byte[] createLabReport(LabOrder order) {
    return createLabReport(order, null);
  }

  public byte[] createLabReport(LabOrder order, Long testId) {
    try (var out = new ByteArrayOutputStream()) {
      var document = new Document(PageSize.A4, 42, 42, 90, 42);
      PdfWriter.getInstance(document, out);
      document.open();

      Font label = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
      Font value = FontFactory.getFont(FontFactory.HELVETICA, 9);
      Font heading = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
      Font section = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

      PdfPTable patient = new PdfPTable(new float[] {18, 32, 18, 32});
      patient.setWidthPercentage(100);
      addMeta(patient, "Patient Name", order.getPatient().getName(), label, value);
      addMeta(patient, "Lab No.", order.getOrderNumber(), label, value);
      addMeta(
          patient,
          "Age / Sex",
          order.getPatient().getAge() + " Years / " + order.getPatient().getGender(),
          label,
          value);
      addMeta(patient, "UHID", order.getPatient().getUhid(), label, value);
      addMeta(
          patient,
          "Referred By",
          order.getReferringDoctor() == null ? "Self referral" : order.getReferringDoctor().getName(),
          label,
          value);
      addMeta(patient, "Priority", Objects.toString(order.getPriority(), "ROUTINE"), label, value);
      document.add(patient);

      for (var test :
          order.getTests().stream()
              .filter(item -> testId == null || Objects.equals(item.getTestId(), testId))
              .toList()) {
        Paragraph department =
            new Paragraph("DEPARTMENT OF " + Objects.toString(test.getDepartment(), "LABORATORY").toUpperCase(), heading);
        department.setAlignment(Element.ALIGN_CENTER);
        department.setSpacingBefore(14);
        document.add(department);
        Paragraph testName = new Paragraph(test.getName().toUpperCase(), heading);
        testName.setAlignment(Element.ALIGN_CENTER);
        testName.setSpacingAfter(7);
        document.add(testName);

        PdfPTable results = new PdfPTable(new float[] {31, 3, 18, 14, 34});
        results.setWidthPercentage(100);
        addResultHeader(results, "INVESTIGATION", label);
        addResultHeader(results, "", label);
        addResultHeader(results, "RESULT", label);
        addResultHeader(results, "UNIT", label);
        addResultHeader(results, "NORMAL INTERVAL", label);

        for (LabResult result : test.getResults()) {
          if (result.getParameterType() == LabParameterType.HEADING) {
            PdfPCell cell = cell(result.getName().toUpperCase(), section);
            cell.setColspan(5);
            cell.setPaddingTop(8);
            results.addCell(cell);
          } else if (result.getParameterType() == LabParameterType.TEXT) {
            results.addCell(cell(result.getName(), value));
            results.addCell(cell(":", label));
            PdfPCell observation = cell(Objects.toString(result.getResult(), "—"), value);
            observation.setColspan(3);
            results.addCell(observation);
          } else {
            results.addCell(cell(result.getName(), value));
            results.addCell(cell(":", label));
            results.addCell(cell(Objects.toString(result.getResult(), "—") + (result.isAbnormal() ? " *" : ""), result.isAbnormal() ? label : value));
            results.addCell(cell(Objects.toString(result.getUnit(), "—"), value));
            results.addCell(cell(Objects.toString(result.getReferenceRange(), "—"), value));
          }
        }
        document.add(results);
      }

      Paragraph remarks = new Paragraph("PATHOLOGIST REMARKS", label);
      remarks.setSpacingBefore(12);
      document.add(remarks);
      document.add(new Paragraph(Objects.toString(order.getRemarks(), "No additional remarks."), value));
      Paragraph end = new Paragraph("— End of Report —", label);
      end.setAlignment(Element.ALIGN_CENTER);
      end.setSpacingBefore(10);
      document.add(end);
      String signatureText =
          order.getSignatureDesignation() == LabSignatureDesignation.CONSULTANT_PATHOLOGIST
              ? "Consultant Pathologist"
              : "Lab Incharge";
      Paragraph signature = new Paragraph(signatureText, label);
      signature.setAlignment(Element.ALIGN_RIGHT);
      signature.setSpacingBefore(24);
      document.add(signature);
      document.close();
      return out.toByteArray();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to generate laboratory report PDF", e);
    }
  }

  private void addMeta(PdfPTable table, String name, String text, Font label, Font value) {
    table.addCell(cell(name, label));
    table.addCell(cell(text, value));
  }

  private void addResultHeader(PdfPTable table, String text, Font font) {
    PdfPCell cell = cell(text, font);
    cell.setBorderWidthTop(0.7f);
    cell.setBorderWidthBottom(0.7f);
    table.addCell(cell);
  }

  private PdfPCell cell(String text, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(Objects.toString(text, ""), font));
    cell.setBorder(Rectangle.NO_BORDER);
    cell.setPadding(4);
    cell.setVerticalAlignment(Element.ALIGN_TOP);
    return cell;
  }
}
