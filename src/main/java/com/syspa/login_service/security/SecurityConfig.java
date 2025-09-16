package com.syspa.login_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syspa.login_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final UserService service;
  private final PasswordEncoder passwordEncoder;
  private final JwtAuthFilter jwtAuthFilter;

  @Value("${ALLOWED_ORIGINS:*}")
  private String allowedOrigins;

  public SecurityConfig(
      UserService service, PasswordEncoder passwordEncoder, JwtAuthFilter jwtAuthFilter) {
    this.service = service;
    this.passwordEncoder = passwordEncoder;
    this.jwtAuthFilter = jwtAuthFilter;
  }

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
    .headers(
      headers -> {
        headers.httpStrictTransportSecurity(
          hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000));
        headers.contentTypeOptions();
        headers.frameOptions(frame -> frame.deny());
        headers.referrerPolicy(
          referrer ->
            referrer.policy(
              org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
                .ReferrerPolicy.NO_REFERRER));
        headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none';"));
      })
    .authorizeHttpRequests(
      registry -> {
        registry
          .requestMatchers("/auth/*/signup", "/auth/*/login", "/auth/*/refresh", "/auth/*/logout")
          .permitAll();
        registry.requestMatchers("/actuator/health", "/actuator/metrics").permitAll();
        registry.requestMatchers("/.well-known/jwks.json", "/.well-known/openid-configuration").permitAll();
        registry.anyRequest().authenticated();
      })
    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
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

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    if ("*".equals(allowedOrigins)) {
      config.addAllowedOriginPattern("*");
    } else {
      for (String origin : allowedOrigins.split(",")) {
        config.addAllowedOrigin(origin.trim());
      }
    }
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
