package es.marta.tfg.carescan.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.UserRepository;

@Controller
public class GeneralController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        if (authentication != null) {
            Optional<User> user = userRepository.findByEmail(authentication.getName());
            if (user.isPresent()) {
                Long userId = user.get().getId();
             
                return "redirect:/" + userId + "/home";
            }
        }
        return "home"; 
    }

    @GetMapping("/login")
    public String inicioSesion() {
        return "login";
    }

    @GetMapping("/signUp")
    public String signUp() {
        return "signUp";
    }

    @PostMapping("/signUp")
    public String procesarRegistro(
            @RequestParam("email") String email,
            @RequestParam("name") String name,
            @RequestParam("password") String password,
            Model model) {

        System.out.println("📥 Recibido registro: " + email);

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "El email ya está en uso");
            return "signUp";
        }

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password));
        newUser.setRole(Role.USER);

        userRepository.save(newUser);


        return "redirect:/login";
    }

  
    @GetMapping("/{id}/home")
    public String userHome(@PathVariable Long id, Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Optional<User> currentUser = userRepository.findByEmail(authentication.getName());

    
        if (currentUser.isEmpty() || !currentUser.get().getId().equals(id)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("username", currentUser.get().getName());
        model.addAttribute("roles", authentication.getAuthorities());
        model.addAttribute("userId", id);

        return "home";
    }
}
