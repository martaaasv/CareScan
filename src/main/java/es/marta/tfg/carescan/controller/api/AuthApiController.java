package es.marta.tfg.carescan.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marta.tfg.carescan.security.jwt.AuthResponse;
import es.marta.tfg.carescan.security.jwt.JwtTokenProvider;

@RestController
@RequestMapping("/api/users")
public class AuthApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public static class LoginRequest {
        public String email;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email, req.password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);

            AuthResponse resp = new AuthResponse(AuthResponse.Status.SUCCESS, "Login correcto");
            resp.setToken(accessToken);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            AuthResponse resp = new AuthResponse(AuthResponse.Status.FAILURE, "Login incorrecto");
            resp.setError("Credenciales inválidas");
            return ResponseEntity.status(401).body(resp);
        }
    }
}