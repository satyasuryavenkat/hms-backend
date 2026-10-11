package com.app.hms.security;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Only an explicit remote false disables application API requests; failures allow access. */
@Service
public class RemoteApiGate {
  private static final Logger log = LoggerFactory.getLogger(RemoteApiGate.class);
  private final String url;
  private final HttpClient client;
  private final LongSupplier clock;
  private boolean enabled;
  private long checkedAt;
  private boolean checked;
  private static final long CACHE_NANOS = Duration.ofSeconds(15).toNanos();

  @Autowired
  public RemoteApiGate(@Value("${app.api-gate.url:}") String url) {
    this(url, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
        .followRedirects(HttpClient.Redirect.NORMAL).build(), System::nanoTime);
  }

  RemoteApiGate(String url, HttpClient client, LongSupplier clock) {
    this.url = url;
    this.client = client;
    this.clock = clock;
  }

  public synchronized boolean isEnabled() {
    if (checked && clock.getAsLong() - checkedAt < CACHE_NANOS) {
      return enabled;
    }
    enabled = true;
    try {
      URI uri = URI.create(url);
      if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
        throw new IllegalArgumentException("An HTTPS flag download URL is required");
      }
      var request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5))
          .header("Cache-Control", "no-cache, no-store").GET().build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      boolean explicitlyDisabled = response.statusCode() == 200 && response.body() != null
          && response.body().length() <= 128
          && response.body().replaceFirst("^\\uFEFF", "").trim()
              .matches("isEnabled\\s*=\\s*false");
      enabled = !explicitlyDisabled;
      if (!enabled) {
        log.warn("Application APIs disabled: remote flag is explicitly false");
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      log.warn("Application APIs allowed: remote flag check interrupted");
    } catch (Exception ex) {
      // Do not log the URL or response: a share link can contain access information.
      log.warn("Application APIs allowed: remote flag could not be read ({})",
          ex.getClass().getSimpleName());
    }
    checkedAt = clock.getAsLong();
    checked = true;
    return enabled;
  }
}
