package io.soqlexplorer.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.soqlexplorer.application.auth.AuthenticateUserUseCase;
import io.soqlexplorer.application.auth.AuthenticatedUser;
import io.soqlexplorer.application.auth.InvalidCredentialsException;
import io.soqlexplorer.domain.user.Email;
import io.soqlexplorer.domain.user.Role;
import io.soqlexplorer.domain.user.UserId;
import io.soqlexplorer.web.security.JwtAuthenticationFilter;
import io.soqlexplorer.web.security.JwtService;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean AuthenticateUserUseCase authenticate;
  @MockBean JwtService jwtService;

  // The persistence-layer adapters require a real datasource at app startup; mock them out.
  @MockBean io.soqlexplorer.application.ports.user.UserRepositoryPort userRepo;
  @MockBean io.soqlexplorer.application.ports.connection.ConnectionRepositoryPort connRepo;
  @MockBean io.soqlexplorer.application.ports.query.QueryHistoryRepositoryPort historyRepo;
  @MockBean io.soqlexplorer.application.ports.query.QueryTemplateRepositoryPort templateRepo;
  @MockBean io.soqlexplorer.application.ports.clock.ClockPort clock;
  @MockBean JwtAuthenticationFilter jwtFilter;

  @Test
  void login_returns_access_token_and_sets_refresh_cookie() throws Exception {
    AuthenticatedUser user =
        new AuthenticatedUser(UserId.of(UUID.randomUUID()), Email.of("u@example.com"), EnumSet.of(Role.USER));
    when(authenticate.authenticate(any(), eq("pw"))).thenReturn(user);
    when(jwtService.issueAccessToken(any()))
        .thenReturn(new JwtService.IssuedToken("access.jwt.value", Instant.parse("2026-05-11T01:00:00Z")));
    when(jwtService.issueRefreshToken(any()))
        .thenReturn(new JwtService.IssuedToken("refresh.jwt.value", Instant.parse("2026-05-18T00:00:00Z")));

    String body = objectMapper.writeValueAsString(new LoginRequest("u@example.com", "pw"));

    mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access.jwt.value"))
        .andExpect(jsonPath("$.email").value("u@example.com"))
        .andExpect(cookie().httpOnly("refresh_token", true))
        .andExpect(cookie().secure("refresh_token", true));
  }

  @Test
  void login_invalid_credentials_returns_401_problem_detail() throws Exception {
    when(authenticate.authenticate(any(), any())).thenThrow(new InvalidCredentialsException());

    String body = objectMapper.writeValueAsString(new LoginRequest("u@example.com", "bad"));

    mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.title").value("invalid-credentials"));
  }

  @Test
  void login_validation_failure_returns_400() throws Exception {
    String body = objectMapper.writeValueAsString(new LoginRequest("not-an-email", ""));
    int statusCode =
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andReturn()
            .getResponse()
            .getStatus();
    assertThat(statusCode).isEqualTo(400);
  }
}
