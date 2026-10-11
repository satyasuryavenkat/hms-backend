package com.app.hms.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class RemoteApiGateTests {
  @Test
  @SuppressWarnings("unchecked")
  void cachesThenDisablesAndRecoversWithoutRestart() throws Exception {
    var client = mock(HttpClient.class);
    HttpResponse<String> response = mock(HttpResponse.class);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn("isEnabled=true");
    var clock = new AtomicLong();
    var gate = new RemoteApiGate("https://drive.google.com/download", client, clock::get);
    assertTrue(gate.isEnabled());
    when(response.body()).thenReturn("isEnabled=false");
    assertTrue(gate.isEnabled());
    verify(client, times(1)).send(any(), any(HttpResponse.BodyHandler.class));
    clock.addAndGet(Duration.ofSeconds(15).toNanos());
    assertFalse(gate.isEnabled());
    when(response.body()).thenReturn("\uFEFFisEnabled = true\r\n");
    clock.addAndGet(Duration.ofSeconds(15).toNanos());
    assertTrue(gate.isEnabled());
    when(client.send(any(), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException());
    clock.addAndGet(Duration.ofSeconds(15).toNanos());
    assertTrue(gate.isEnabled(), "A failed refresh allows access");
  }

  @Test
  @SuppressWarnings("unchecked")
  void allowsUnavailableOrInvalidFlags() throws Exception {
    var client = mock(HttpClient.class);
    HttpResponse<String> response = mock(HttpResponse.class);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
    when(response.statusCode()).thenReturn(200);
    for (String body : new String[] {"", "true", "isEnabled=TRUE", "<html>isEnabled=true</html>",
        "isEnabled=true\nisEnabled=false", "isEnabled=true\nother=value"}) {
      when(response.body()).thenReturn(body);
      assertTrue(new RemoteApiGate("https://drive.google.com/download", client, () -> 0).isEnabled());
    }
    when(response.body()).thenReturn("isEnabled=true");
    for (int status : new int[] {403, 404, 429, 500, 503}) {
      when(response.statusCode()).thenReturn(status);
      assertTrue(new RemoteApiGate("https://drive.google.com/download", client, () -> 0).isEnabled());
    }
    assertTrue(new RemoteApiGate("", client, () -> 0).isEnabled());
    assertTrue(new RemoteApiGate("http://example.com/flag", client, () -> 0).isEnabled());
  }

  @Test
  @SuppressWarnings("unchecked")
  void unavailableFileReleasesPreviouslyDisabledApisAndLaterFalseBlocksAgain() throws Exception {
    var client = mock(HttpClient.class);
    HttpResponse<String> response = mock(HttpResponse.class);
    when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn("isEnabled=false");
    var clock = new AtomicLong();
    var gate = new RemoteApiGate("https://drive.google.com/download", client, clock::get);
    assertFalse(gate.isEnabled());
    when(response.statusCode()).thenReturn(404);
    assertFalse(gate.isEnabled(), "Cached false lasts until expiry");
    clock.addAndGet(Duration.ofSeconds(15).toNanos());
    assertTrue(gate.isEnabled());
    when(response.statusCode()).thenReturn(200);
    clock.addAndGet(Duration.ofSeconds(15).toNanos());
    assertFalse(gate.isEnabled());
    when(client.send(any(), any(HttpResponse.BodyHandler.class)))
        .thenThrow(new java.net.http.HttpTimeoutException("timeout"));
    clock.addAndGet(Duration.ofSeconds(15).toNanos());
    assertTrue(gate.isEnabled());
  }
}
