package com.manus.gamemap.controller;

import com.manus.gamemap.dto.*;
import com.manus.gamemap.model.User;
import com.manus.gamemap.repository.UserRepository;
import com.manus.gamemap.security.JwtUtil;
import com.manus.gamemap.service.CustomUserDetailsService;
import com.manus.gamemap.service.SessionService;
import com.manus.gamemap.service.TotpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TotpService totpService;

    @Autowired
    private SessionService sessionService;

    // Temporary storage for users pending 2FA verification: tempToken -> username
    private final Map<String, String> pending2FA = new ConcurrentHashMap<>();

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (Exception e) {
            throw new Exception("Incorrect username or password", e);
        }

        User user = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isTwoFactorEnabled()) {
            String tempToken = UUID.randomUUID().toString();
            pending2FA.put(tempToken, user.getUsername());
            return ResponseEntity.ok(new TwoFactorResponse(true, "2FA required", tempToken));
        }

        // No 2FA, proceed to login
        return completeLogin(user);
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verifyTwoFactor(@RequestBody VerifyCodeRequest request) {
        String username = pending2FA.get(request.getTempToken());
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!totpService.verifyCode(user.getTwoFactorSecret(), request.getCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid 2FA code");
        }

        pending2FA.remove(request.getTempToken());
        return completeLogin(user);
    }

    private ResponseEntity<?> completeLogin(User user) {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        final String sessionId = sessionService.createSession(user.getId());

        return ResponseEntity.ok(new AuthResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities().iterator().next().getAuthority(), sessionId));
    }

    @GetMapping("/2fa/status")
    public ResponseEntity<?> getTwoFactorStatus(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        User user = userRepository.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(Map.of("enabled", user.isTwoFactorEnabled()));
    }

    @GetMapping("/2fa/setup")
    public ResponseEntity<?> setupTwoFactor(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        User user = userRepository.findByUsername(username).orElseThrow();
        
        String secret = totpService.generateSecret();
        String qrCodeUri = totpService.getQrCodeImageUri(secret, username);
        
        // We don't save the secret yet, user must verify first
        return ResponseEntity.ok(new QrCodeResponse(qrCodeUri, secret));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<?> enableTwoFactor(@RequestHeader("Authorization") String token, @RequestBody VerifyCodeRequest request) {
        String username = jwtUtil.extractUsername(token.substring(7));
        User user = userRepository.findByUsername(username).orElseThrow();

        // Here request.getTempToken() is actually the secret sent from frontend
        if (!totpService.verifyCode(request.getTempToken(), request.getCode())) {
            return ResponseEntity.badRequest().body("Invalid code");
        }

        user.setTwoFactorSecret(request.getTempToken());
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok("2FA enabled successfully");
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disableTwoFactor(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        User user = userRepository.findByUsername(username).orElseThrow();

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);

        return ResponseEntity.ok("2FA disabled successfully");
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "X-Session-ID", required = false) String sessionId) {
        if (sessionId != null) {
            sessionService.invalidateSession(sessionId);
        }
        return ResponseEntity.ok("Logged out successfully");
    }
}
