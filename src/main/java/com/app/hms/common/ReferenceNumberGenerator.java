package com.app.hms.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class ReferenceNumberGenerator {
  private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.ofPattern("yyMMdd");

  public String dated(String prefix, Long id) {
    return "%s-%s-%03d".formatted(prefix, LocalDate.now().format(COMPACT_DATE), id);
  }

  public String yearly(String prefix, Long id) {
    return "%s-%d-%04d".formatted(prefix, LocalDate.now().getYear(), id);
  }
}
