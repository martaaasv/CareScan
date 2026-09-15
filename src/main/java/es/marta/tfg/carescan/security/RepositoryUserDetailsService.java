package es.marta.tfg.carescan.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import es.marta.tfg.carescan.model.Estado;
import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.UserRepository;

@Service
public class RepositoryUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        if (user.getBlockedUntil() != null && !user.isBlockedManually()
                && !user.getBlockedUntil().isAfter(java.time.LocalDateTime.now())) {
            user.setBlockedUntil(null);
            userRepository.save(user);
        }

        boolean enabled = user.isActive()
                && !user.isBlocked()
                && !(user.getRole() == Role.PACIENTE && user.getEstado() == Estado.INACTIVO);
 
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!enabled)
                .build();
    }
}
