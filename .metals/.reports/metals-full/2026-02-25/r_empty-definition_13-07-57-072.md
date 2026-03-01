error id: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/service/userService.java:_empty_/JwtTokenProvider#
file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/service/userService.java
empty definition using pc, found symbol in pc: _empty_/JwtTokenProvider#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1209
uri: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/service/userService.java
text:
```scala
package es.marta.tfg.carescan.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import es.marta.tfg.carescan.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;

import es.marta.tfg.carescan.security.jwt.AuthResponse;
import es.marta.tfg.carescan.security.jwt.JwtTokenProvider;
import es.marta.tfg.carescan.security.jwt.TokenType;


@Service
public class userService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Jw@@tTokenProvider jwtTokenProvider;


    public ResponseEntity<String> login(String name, String password, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(name, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return ResponseEntity.ok("Inicio de sesión exitoso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error de autenticación: " + e.getMessage());
        }
    }

    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        try {

            Cookie refreshCookie = new Cookie(TokenType.REFRESH.cookieName, null);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(0);
            response.addCookie(refreshCookie);

            Cookie accessCookie = new Cookie(TokenType.ACCESS.cookieName, null);
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(0);
            response.addCookie(accessCookie);

            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Logout successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(AuthResponse.Status.FAILURE, "Logout failed", e.getMessage()));
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/JwtTokenProvider#