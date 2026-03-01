package es.marta.tfg.carescan.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.marta.tfg.carescan.model.Estado;
import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.ConsultaRepository;
import es.marta.tfg.carescan.repository.UserRepository;
import es.marta.tfg.carescan.service.HardDeleteUserService;
import es.marta.tfg.carescan.service.PatientDeletionService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private HardDeleteUserService hardDeleteUserService;

    @Autowired
    private PatientDeletionService patientDeletionService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Authentication auth) {

        List<User> users = userRepository.findAll();

        model.addAttribute("users", users);

        long totalUsuarios = users.size();
        long totalConsultas = consultaRepository.count();

        Map<Long, Long> consultasCount = new LinkedHashMap<>();
        for (User u : users) {
            consultasCount.put(u.getId(), consultaRepository.countByUser(u));
        }

        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalConsultas", totalConsultas);
        model.addAttribute("consultasCount", consultasCount);

        User admin = userRepository.findByEmail(auth.getName()).get();
        model.addAttribute("userId", admin.getId());
        model.addAttribute("username", admin.getName());

        return "admin-dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model, Authentication auth) {

        List<User> users = userRepository.findAll();

        Map<Long, Long> consultasCount = new LinkedHashMap<>();
        for (User u : users) {
            long count = consultaRepository.countByUser(u);
            consultasCount.put(u.getId(), count);
        }

        model.addAttribute("users", users);
        model.addAttribute("consultasCount", consultasCount);
        User admin = userRepository.findByEmail(auth.getName()).get();
        model.addAttribute("userId", admin.getId());
        model.addAttribute("username", admin.getName());

        return "admin-users";
    }

    @PostMapping("/users/{id}/role")
    public String changeUserRole(
            @PathVariable Long id,
            @RequestParam("role") Role role,
            RedirectAttributes redirectAttributes) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El usuario no existe.");
            return "redirect:/admin/users";
        }

        User user = optionalUser.get();
        user.setRole(role);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("mensajeExito", "Rol actualizado correctamente.");
        return "redirect:/admin/users";
    }

    /*   @PostMapping("/users/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El usuario no existe.");
            return "redirect:/admin/users";
        }

        User user = optionalUser.get();

        consultaRepository.deleteByUser(user);
        userRepository.delete(user);

        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario borrado correctamente.");
        return "redirect:/admin/users";
    }*/
    @PostMapping("/patients/{patientId}/delete")
    public String deletePatient(@PathVariable Long patientId, RedirectAttributes ra) {

        User patient = userRepository.findById(patientId).orElse(null);
        if (patient == null || patient.getRole() != Role.PACIENTE) {
            ra.addFlashAttribute("error", "Paciente no encontrado.");
            return "redirect:/admin-hospital/patients";
        }

        // Solo permitimos borrar duro si ya está INACTIVO
        if (patient.getEstado() != Estado.INACTIVO) {
            ra.addFlashAttribute("error", "Solo se puede eliminar definitivamente un paciente INACTIVO.");
            return "redirect:/admin-hospital/patients";
        }

        patientDeletionService.hardDeletePatient(patientId);

        ra.addFlashAttribute("success", "Paciente eliminado definitivamente (consultas, radiografías, asignaciones, etc.).");
        return "redirect:/admin-hospital/patients";
    }

    @PostMapping("/users/{id}/hard-delete")
    public String hardDeleteUser(@PathVariable Long id, RedirectAttributes ra) {

        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("mensajeError", "El usuario no existe.");
            return "redirect:/admin/users";
        }

        try {
            hardDeleteUserService.hardDeleteUser(id);
            ra.addFlashAttribute("mensajeExito", "Usuario eliminado definitivamente (borrado completo).");
        } catch (Exception e) {
            ra.addFlashAttribute("mensajeError", "No se pudo eliminar el usuario. Revisa claves foráneas o relaciones: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
