error id: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/SampleData.java:_empty_/User#
file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/SampleData.java
empty definition using pc, found symbol in pc: _empty_/User#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 678
uri: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/SampleData.java
text:
```scala
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
                
                @@User admin = new User();
                admin.setName("Admin");
                admin.setEmail("adminIT@carescan.com");
                admin.setPassword(new BCryptPasswordEncoder().encode("1234admin"));
                admin.setRole(Role.ADMIN_IT);
                userRepository.save(admin);

                User user1 = new User();
                user1.setName("User");
                user1.setEmail("user@gmail.com");
                user1.setPassword(new BCryptPasswordEncoder().encode("1234user"));
                user1.setRole(Role.MEDICO);
                userRepository.save(user1);


            }

        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(SampleData.class).error("Unexpected runtime error occurred while initializing sample data", e);
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/User#