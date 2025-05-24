package com.example.eshop.controller;

import com.example.eshop.model.User;
import com.example.eshop.model.dto.auth.AuthDto;
import com.example.eshop.model.dto.auth.RegisterDto;
import com.example.eshop.security.util.CustomUserDetails;
import com.example.eshop.service.PasswordValidationService;
import com.example.eshop.service.RegisterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

// import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final RegisterService registerService;
    private final PasswordValidationService passwordValidationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto loginRequest, HttpServletRequest request,
                                   HttpServletResponse response) {
        Authentication authentication = authenticateUserAndCreateSession(
                loginRequest.email(), loginRequest.password(), request);

        return ResponseEntity.ok(Map.of(
                "message", "Login successful"));
    }

    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody RegisterDto registerRequest, HttpServletRequest request,
                                      HttpServletResponse response){
        passwordValidationService.validatePasswords(registerRequest);
        User user = registerService.registerCustomer(registerRequest);

        // Authenticate and set ownership
        Authentication authentication = authenticateUserAndCreateSession(user.getEmail(), registerRequest.password(), request);
    
        return ResponseEntity.ok(Map.of(
                "message", "Register successful",
                "details",((CustomUserDetails) authentication.getPrincipal())));
    }

    private Authentication authenticateUserAndCreateSession(String email, String rawPassword, HttpServletRequest request) {
        Authentication auth = new UsernamePasswordAuthenticationToken(email, rawPassword);
        Authentication authenticated = authenticationManager.authenticate(auth);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authenticated);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        return authenticated;
    }
}
