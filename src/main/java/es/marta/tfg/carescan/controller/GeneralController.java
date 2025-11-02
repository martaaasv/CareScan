package es.marta.tfg.carescan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.UserRepository;

@Controller
public class GeneralController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String homeTest() {
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
        newUser.setRoles(java.util.List.of("USER"));

        userRepository.save(newUser);

        System.out.println("Usuario guardado correctamente");

        return "redirect:/login";
    }

    @GetMapping("/userHome")
    public String userHome() {
        return "userHome";
    }

}
