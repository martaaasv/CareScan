package es.marta.tfg.carescan.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.UserRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Controller
public class GeneralController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ===== DTO interno para el formulario de registro =====
    public static class SignUpForm {
        @NotBlank(message = "El nombre es obligatorio")
        private String name;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email no válido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

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

    // ===== Registro (GET) usando PRG y DTO =====
    @GetMapping("/signUp")
    public String signUp(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new SignUpForm());
        }
        return "signUp";
    }

    // ===== Registro (POST) con validaciones y “email ya existe” =====
    @PostMapping("/signUp")
    public String procesarRegistro(
            @Valid @ModelAttribute("form") SignUpForm form,
            BindingResult binding,
            RedirectAttributes ra) {

        if (userRepository.existsByEmail(form.getEmail())) {
            binding.rejectValue("email", "exists", "Ya existe una cuenta con este email");
        }

        if (binding.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", binding);
            ra.addFlashAttribute("form", form);
            return "redirect:/signUp";
        }

        User newUser = new User();
        newUser.setName(form.getName());
        newUser.setEmail(form.getEmail());
        newUser.setPassword(passwordEncoder.encode(form.getPassword()));
        newUser.setRole(Role.USER);
        userRepository.save(newUser);

        return "redirect:/login?registered=true";
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
