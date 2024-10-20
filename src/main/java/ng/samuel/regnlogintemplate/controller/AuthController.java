package ng.samuel.regnlogintemplate.controller;


import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ng.samuel.regnlogintemplate.config.JwtService;
import ng.samuel.regnlogintemplate.payload.request.LoginDto;
import ng.samuel.regnlogintemplate.payload.request.RegistrationDto;
import ng.samuel.regnlogintemplate.payload.response.LoginResponse;
import ng.samuel.regnlogintemplate.payload.response.RegistrationResponse;
import ng.samuel.regnlogintemplate.service.AuthService;
import ng.samuel.regnlogintemplate.service.ConfirmationTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final ConfirmationTokenService confirmationTokenService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerUser(@Valid @RequestBody RegistrationDto request) {

        try {
            RegistrationResponse registerUser = authService.register(request);
            if (!registerUser.equals("Invalid Email domain")) {
                return ResponseEntity.ok(registerUser);
            } else {
                return ResponseEntity.badRequest().body(registerUser);
            }
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login-user")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginDto loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }


    @GetMapping("/confirm")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") String token) {

        String result = confirmationTokenService.validateToken(token);
        if ("Email confirmed successfully".equals(result)) {
            return ResponseEntity.ok(Collections.singletonMap("message", result));
        } else {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", result));
        }

    }
}
