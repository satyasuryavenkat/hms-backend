package com.app.hms.common;

import org.springframework.data.domain.PageRequest;

public final class PageUtils {
  private static final int MAX_PAGE_SIZE = 100;

  private PageUtils() {}

  public static PageRequest request(int page, int size) {
    return PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
  }

  public static String query(String query) {
    return query == null ? "" : query.trim();
  }
}
