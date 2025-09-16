package com.syspa.login_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

@RestController
@RequestMapping("/.well-known")
public class WellKnownController {

    // RSA modulus and exponent from environment/config for JWKS
    @Value("${jwt.rsaModulus:sXchQEXAMPLEMODULUS1234567890abcdefg...}")
    private String rsaModulus;

    @Value("${jwt.rsaExponent:AQAB}")
    private String rsaExponent;

    @Value("${jwt.issuer:http://localhost:8080}")
    private String issuer;

    @GetMapping(value = "/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> jwks() {
    // JWKS endpoint: publishes RSA public key for JWT validation
    // Values are injected from environment/config, ready for production
    Map<String, Object> jwk = new HashMap<>();
    jwk.put("kty", "RSA");
    jwk.put("alg", "RS256");
    jwk.put("use", "sig");
    jwk.put("kid", "default");
    jwk.put("n", rsaModulus); // Base64URL encoded modulus
    jwk.put("e", rsaExponent); // Base64URL encoded exponent
    Map<String, Object> jwks = new HashMap<>();
    jwks.put("keys", List.of(jwk));
    return ResponseEntity.ok(jwks);
    }

    @GetMapping(value = "/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> openidConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("issuer", issuer);
        config.put("jwks_uri", issuer + "/.well-known/jwks.json");
        config.put("id_token_signing_alg_values_supported", List.of("HS256"));
        config.put("token_endpoint", issuer + "/auth/V1/login");
        config.put("authorization_endpoint", issuer + "/auth/V1/login");
        config.put("response_types_supported", List.of("code", "token", "id_token"));
        config.put("subject_types_supported", List.of("public"));
        return ResponseEntity.ok(config);
    }
}
