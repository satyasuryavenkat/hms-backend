package com.app.hms.service;

import com.lowagie.text.*;
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
}
