package es.marta.tfg.carescan.controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import es.marta.tfg.carescan.model.AuditLog;
import es.marta.tfg.carescan.repository.AuditLogRepository;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogRepository auditLogRepository;

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
    public String listUsers(@RequestParam(value = "role", required = false) String roleFilter, Model model, Authentication auth) {

        List<User> users = userRepository.findAll();

        if (roleFilter != null && !roleFilter.isBlank()) {
            try {
                Role selectedRole = Role.valueOf(roleFilter);
                users = users.stream()
                        .filter(u -> u.getRole() == selectedRole)
                        .toList();
            } catch (IllegalArgumentException ignored) {
                roleFilter = "";
            }
        }

        Map<Long, Long> consultasCount = new LinkedHashMap<>();
        for (User u : users) {
            long count = consultaRepository.countByUser(u);
            consultasCount.put(u.getId(), count);
        }

        model.addAttribute("users", users);
        model.addAttribute("consultasCount", consultasCount);
        model.addAttribute("selectedRole", roleFilter == null ? "" : roleFilter);
        User admin = userRepository.findByEmail(auth.getName()).get();
        model.addAttribute("userId", admin.getId());
        model.addAttribute("username", admin.getName());

        return "admin-users";
    }

    @GetMapping("/logs")
    public String auditLogs(
            @RequestParam(value = "actor", required = false) String actor,
            @RequestParam(value = "path", required = false) String path,
            @RequestParam(value = "method", required = false) String method,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            Authentication auth) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), 25);
        Page<AuditLog> logs = auditLogRepository.search(
                blankToNull(actor),
                blankToNull(path),
                blankToNull(method),
                status,
                pageable);

        User admin = userRepository.findByEmail(auth.getName()).get();
        model.addAttribute("userId", admin.getId());
        model.addAttribute("username", admin.getName());
        model.addAttribute("logs", logs);
        model.addAttribute("actor", actor == null ? "" : actor);
        model.addAttribute("path", path == null ? "" : path);
        model.addAttribute("method", method == null ? "" : method);
        model.addAttribute("status", status);

        return "admin-logs";
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

    @PostMapping("/users/{id}/block")
    public String blockUser(
            @PathVariable Long id,
            @RequestParam("blockType") String blockType,
            RedirectAttributes redirectAttributes) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El usuario no existe.");
            return "redirect:/admin/users";
        }

        User user = optionalUser.get();
        user.setBlockedManually(false);
        user.setBlockedUntil(null);

        switch (blockType) {
            case "1m" -> user.setBlockedUntil(LocalDateTime.now().plusMinutes(1));
            case "1d" -> user.setBlockedUntil(LocalDateTime.now().plusDays(1));
            case "7d" -> user.setBlockedUntil(LocalDateTime.now().plusDays(7));
            case "manual" -> user.setBlockedManually(true);
            default -> {
                redirectAttributes.addFlashAttribute("mensajeError", "Tipo de bloqueo no valido.");
                return "redirect:/admin/users";
            }
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario bloqueado correctamente.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unblock")
    public String unblockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El usuario no existe.");
            return "redirect:/admin/users";
        }

        User user = optionalUser.get();
        user.setBlockedManually(false);
        user.setBlockedUntil(null);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario desbloqueado correctamente.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El usuario no existe.");
            return "redirect:/admin/users";
        }

        User user = optionalUser.get();
        String tempPassword = generateTempPassword(10);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setTemporalPassword(true);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("mensajeExito", "Contrasena restablecida correctamente.");
        redirectAttributes.addFlashAttribute("tempPassword", tempPassword);
        redirectAttributes.addFlashAttribute("resetPasswordEmail", user.getEmail());
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

    private String generateTempPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
