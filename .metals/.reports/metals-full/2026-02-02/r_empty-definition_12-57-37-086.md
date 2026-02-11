error id: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/GeneralController.java:_empty_/RedirectAttributes#addFlashAttribute#
file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/GeneralController.java
empty definition using pc, found symbol in pc: _empty_/RedirectAttributes#addFlashAttribute#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 6110
uri: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/GeneralController.java
text:
```scala
package es.marta.tfg.carescan.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.marta.tfg.carescan.DTO.signUp;
import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
public class GeneralController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    // @GetMapping("/signUp")
    // public String signUp(Model model) {
    //     if (!model.containsAttribute("form")) {
    //         model.addAttribute("form", new signUp());
    //     }
    //     return "signUp";
    // }

   
    // @PostMapping("/signUp")
    // public String procesarRegistro(
    //         @Valid @ModelAttribute("form") signUp form,
    //         BindingResult binding,
    //         RedirectAttributes ra) {

    //     if (userRepository.existsByEmail(form.getEmail())) {
    //         binding.rejectValue("email", "exists", "Ya existe una cuenta con este email");
    //     }

    //     if (binding.hasErrors()) {
    //         ra.addFlashAttribute("org.springframework.validation.BindingResult.form", binding);
    //         ra.addFlashAttribute("form", form);
    //         return "redirect:/signUp";
    //     }

    //     User newUser = new User();
    //     newUser.setName(form.getName());
    //     newUser.setEmail(form.getEmail());
    //     newUser.setPassword(passwordEncoder.encode(form.getPassword()));
    //     newUser.setRole(Role.USER);
    //     userRepository.save(newUser);

    //     return "redirect:/login";
    // }

    @GetMapping("/{id}/home")
    public String userHome(@PathVariable Long id,
            Authentication authentication,
            Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Optional<User> currentUser
                = userRepository.findByEmail(authentication.getName());
        if (currentUser.isEmpty() || !currentUser.get().getId().equals(id)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("username", currentUser.get().getName());
        model.addAttribute("roles", authentication.getAuthorities());
        model.addAttribute("userId", id);

        return "home";
    }

    @GetMapping("/{id}/settings")
    public String userSettings(@PathVariable Long id,
            Authentication authentication,
            Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Optional<User> currentUser
                = userRepository.findByEmail(authentication.getName());
        if (currentUser.isEmpty() || !currentUser.get().getId().equals(id)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("username", currentUser.get().getName());
        model.addAttribute("userId", id);
        return "settings";
    }

    @PostMapping("/{id}/change-password")
    public String changePassword(@PathVariable Long id,
            Authentication authentication,
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes ra) {

        if (authentication == null) {
            return "redirect:/login";
        }

        Optional<User> currentUserOpt = userRepository.findByEmail(authentication.getName());
        if (currentUserOpt.isEmpty() || !currentUserOpt.get().getId().equals(id)) {
            return "redirect:/access-denied";
        }

        User currentUser = currentUserOpt.get();

        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            ra.addFlashAttribute("passwordError",
                    "La contraseña actual no es correcta.");
            return "redirect:/" + id + "/settings";
        }

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("passwordError",
                    "Las contraseñas nuevas no coinciden.");
            return "redirect:/" + id + "/settings";
        }

        if (newPassword.length() < 8) {
            ra.addFlashAttribute("passwordError",
                    "La nueva contraseña debe tener al menos 8 caracteres.");
            return "redirect:/" + id + "/settings";
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        currentUser.setTemporalPassword(false);
        userRepository.save(currentUser);

        ra.addFlashAttr@@ibute("passwordSuccess",
                "Contraseña actualizada correctamente.");
        return "redirect:/" + id + "/settings";
    }

    @PostMapping("/{id}/delete-account")
    public String deleteAccount(@PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes ra) {

        if (authentication == null) {
            return "redirect:/login";
        }

        Optional<User> currentUserOpt
                = userRepository.findByEmail(authentication.getName());
        if (currentUserOpt.isEmpty() || !currentUserOpt.get().getId().equals(id)) {
            return "redirect:/access-denied";
        }

        userRepository.delete(currentUserOpt.get());

        try {
            request.logout();
        } catch (Exception e) {

        }
        SecurityContextHolder.clearContext();

        ra.addFlashAttribute("accountDeleted",
                "Tu cuenta ha sido eliminada correctamente.");
        return "redirect:/";
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/RedirectAttributes#addFlashAttribute#