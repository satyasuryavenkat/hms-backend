package com.app.hms.security;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class RemoteApiGateIntegrationTests {
  @MockitoBean RemoteApiGate gate;
  @Autowired WebApplicationContext context;
  MockMvc mvc;

  @BeforeEach
  void setup() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void blocksLoginRefreshAndProtectedApisBeforeAuthentication() throws Exception {
    when(gate.isEnabled()).thenReturn(false);
    for (String path : new String[] {"/api/v1/auth/login", "/api/v1/auth/refresh",
        "/api/v1/patients", "/api/v1/lab-orders/1/report.pdf", "/api/v1/unknown"}) {
      mvc.perform(post(path).servletPath(path).header("Authorization", "Bearer invalid"))
          .andExpect(status().isServiceUnavailable())
          .andExpect(jsonPath("$.code").value("APPLICATION_DISABLED"));
    }
    mvc.perform(get("/api/v1/patients").servletPath("/api/v1/patients"))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void trueStillRequiresNormalAuthenticationAndHealthRemainsAccessible() throws Exception {
    when(gate.isEnabled()).thenReturn(true);
    mvc.perform(get("/api/v1/patients").servletPath("/api/v1/patients"))
        .andExpect(status().isForbidden());
    when(gate.isEnabled()).thenReturn(false);
    mvc.perform(get("/actuator/health").servletPath("/actuator/health"))
        .andExpect(status().isOk());
  }
}
