package es.marta.tfg.carescan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Service
public class SampleData {

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() throws java.io.IOException {
        try {
            if (userRepository.count() == 0) {
                
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@carescan.com");
                admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);

                User user1 = new User();
                user1.setName("User");
                user1.setEmail("user@gmail.com");
                user1.setPassword(new BCryptPasswordEncoder().encode("1234"));
                user1.setRole(Role.USER);
                userRepository.save(user1);


            }

        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(SampleData.class).error("Unexpected runtime error occurred while initializing sample data", e);
        }
    }
}
