package com.example.eshop.controller;

import com.example.eshop.model.User;
import com.example.eshop.model.dto.auth.AuthRequest;
import com.example.eshop.model.dto.auth.RegisterRequest;
import com.example.eshop.model.dto.common.SuccessResponse;
import com.example.eshop.service.RegisterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final RegisterService registerService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticateUserAndCreateSession(
                loginRequest.email(), loginRequest.password(), request);

        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Login successful"));
    }

    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request, HttpServletResponse response){
        User user = registerService.registerCustomer(registerRequest);

        // Authenticate and set ownership
        Authentication authentication = authenticateUserAndCreateSession(user.getEmail(), registerRequest.passwordFields().password(), request);
    
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Register successful"));
    }

    @GetMapping("/user-role")
    public ResponseEntity<?> getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        Optional<? extends GrantedAuthority> roleOpt = authentication.getAuthorities().stream().findFirst();

        if (roleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Role not found"));
        }

        String role = roleOpt.get().getAuthority().replace("ROLE_", "");

        return ResponseEntity.ok(Map.of("role", role));
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
