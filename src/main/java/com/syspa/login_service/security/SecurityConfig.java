package com.syspa.login_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syspa.login_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

  @Autowired private final UserService service;
  @Autowired private final PasswordEncoder passwordEncoder;

  @Bean
  public UserDetailsService userDetailsService() {
    return service;
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(service);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
  return httpSecurity
    .csrf(AbstractHttpConfigurer::disable)
    .headers(headers -> {
      headers.httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000)
      );
      headers.contentTypeOptions();
      headers.frameOptions(frame -> frame.deny());
      headers.referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER));
      headers.contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'none';")
      );
    })
    .authorizeHttpRequests(
      registry -> {
        registry.requestMatchers("/auth/*/signup", "/auth/*/login", "/auth/*/refresh").permitAll();
        registry.requestMatchers("/actuator/health", "/actuator/metrics").permitAll();
        registry.anyRequest().authenticated();
      })
    .build();
  }

  @Bean
  public AuthenticationEntryPoint customAuthenticationEntryPoint() {
    return new AuthenticationEntryPoint() {
      @Override
      public void commence(
          HttpServletRequest request,
          HttpServletResponse response,
          org.springframework.security.core.AuthenticationException authException)
          throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        var error = new java.util.HashMap<String, String>();
        error.put("error", authException.getMessage());
        new ObjectMapper().writeValue(response.getOutputStream(), error);
      }
    };
  }
}
