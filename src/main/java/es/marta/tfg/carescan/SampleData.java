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
                
                User adminIT = new User();
                adminIT.setName("AdminIT");
                adminIT.setEmail("adminIT@carescan.com");
                adminIT.setPassword(new BCryptPasswordEncoder().encode("1234adminit"));
                adminIT.setRole(Role.ADMIN_IT);
                userRepository.save(adminIT);

                User adminH = new User();
                adminH.setName("AdminH");
                adminH.setEmail("adminH@carescan.com");
                adminH.setPassword(new BCryptPasswordEncoder().encode("1234adminh"));
                adminH.setRole(Role.ADMIN_HOSPITAL);
                userRepository.save(adminH);

                User user1 = new User();
                user1.setName("MedicoPrueba");
                user1.setEmail("medico@gmail.com");
                user1.setPassword(new BCryptPasswordEncoder().encode("1234medico"));
                user1.setRole(Role.MEDICO);
                userRepository.save(user1);


            }

        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(SampleData.class).error("Unexpected runtime error occurred while initializing sample data", e);
        }
    }
}
