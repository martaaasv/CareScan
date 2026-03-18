package es.marta.tfg.carescan.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.marta.tfg.carescan.security.jwt.AuthResponse;
import es.marta.tfg.carescan.security.jwt.LoginRequest;
import es.marta.tfg.carescan.security.jwt.UserLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/users")
public class AuthRestController {

    private final UserLoginService userLoginService;

    public AuthRestController(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        return userLoginService.login(response, loginRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        String result = userLoginService.logout(response);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        if (request.getCookies() == null) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(AuthResponse.Status.FAILURE, "No cookies found"));
        }

        String refreshToken = null;
        for (var cookie : request.getCookies()) {
            if ("RefreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(AuthResponse.Status.FAILURE, "Refresh token not found"));
        }

        return userLoginService.refresh(response, refreshToken);
    }
}