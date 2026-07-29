package com.app.hms.common;

import org.springframework.http.*;

public final class PdfResponseFactory {
  private PdfResponseFactory() {}

  public static ResponseEntity<byte[]> attachment(String filename, byte[] content) {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .body(content);
  }
}
