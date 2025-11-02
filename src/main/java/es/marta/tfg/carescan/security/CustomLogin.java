package es.marta.tfg.carescan.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLogin implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectURL = request.getContextPath();

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            if ("ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
                redirectURL = request.getContextPath() + "/adminHome";
                break;
            } else if ("ROLE_USER".equalsIgnoreCase(role) || "USER".equalsIgnoreCase(role)) {
                redirectURL = request.getContextPath() + "/userHome";
                break;
            } else {
                redirectURL = request.getContextPath() + "/login?error=true";
            }
        }

        response.sendRedirect(redirectURL);
    }
}
 
