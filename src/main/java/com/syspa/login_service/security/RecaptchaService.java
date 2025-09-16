package com.syspa.login_service.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {

  @Value("${RECAPTCHA_SECRET:6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe}")
  private String recaptchaSecret;

  private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

  public boolean verify(String token) {
    RestTemplate restTemplate = new RestTemplate();
    Map<String, String> body =
        Map.of(
            "secret", recaptchaSecret,
            "response", token);
    RecaptchaResponse response =
        restTemplate.postForObject(
            VERIFY_URL + "?secret={secret}&response={response}",
            null,
            RecaptchaResponse.class,
            body);
    return response != null && response.isSuccess();
  }

  private static class RecaptchaResponse {
    @JsonProperty("success")
    private boolean success;

    public boolean isSuccess() {
      return success;
    }
  }
}
