package es.marta.tfg.carescan.controller;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.UserRepository;

@Controller
@RequestMapping("/admin-hospital")
public class AdminHospitalController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/patients/new")
    public String newPatientForm(Model model, Authentication auth) {

        if (auth != null) {
            User admin = userRepository.findByEmail(auth.getName()).orElse(null);
            if (admin != null) {
                model.addAttribute("userId", admin.getId());
                model.addAttribute("username", admin.getName());
            }
        }

        return "adminHospital/newPatient";
    }

    @PostMapping("/patients")
    public String createPatient(@RequestParam("name") String name,
            @RequestParam("email") String email,
            RedirectAttributes ra) {

        if (userRepository.findByEmail(email).isPresent()) {
            ra.addFlashAttribute("error", "Ya existe un usuario con ese email.");
            return "redirect:/admin-hospital/patients/new";
        }

        String tempPassword = generateTempPassword(10);

        User patient = new User();
        patient.setName(name);
        patient.setEmail(email);
        patient.setRole(Role.PACIENTE);
        patient.setPassword(passwordEncoder.encode(tempPassword));
        patient.setTemporalPassword(true);

        userRepository.save(patient);

        ra.addFlashAttribute("success", "Paciente creado correctamente.");
        ra.addFlashAttribute("tempPassword", tempPassword);
        ra.addFlashAttribute("createdEmail", email);

        return "redirect:/admin-hospital/patients/new";
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

    // para asignar médicos a pacientes 
    @GetMapping("/assignments/new")
    public String newAssignmentForm(Model model, Authentication auth) {

        List<User> doctors = userRepository.findAllByRole(Role.MEDICO);
        List<User> patients = userRepository.findAllByRole(Role.PACIENTE);

        model.addAttribute("doctors", doctors);
        model.addAttribute("patients", patients);

        if (auth != null) {
            userRepository.findByEmail(auth.getName()).ifPresent(admin -> {
                model.addAttribute("userId", admin.getId());
                model.addAttribute("username", admin.getName());
            });
        }

        return "adminHospital/newAssignment";
    }

    @PostMapping("/assignments")
    public String createAssignment(@RequestParam("doctorId") Long doctorId,
            @RequestParam("patientId") Long patientId,
            RedirectAttributes ra) {

        User doctor = userRepository.findById(doctorId).orElse(null);
        User patient = userRepository.findById(patientId).orElse(null);

        if (doctor == null || patient == null) {
            ra.addFlashAttribute("error", "Médico o paciente no encontrado.");
            return "redirect:/admin-hospital/assignments/new";
        }

        if (doctor.getRole() != Role.MEDICO) {
            ra.addFlashAttribute("error", "El usuario seleccionado como médico no es MEDICO.");
            return "redirect:/admin-hospital/assignments/new";
        }

        if (patient.getRole() != Role.PACIENTE) {
            ra.addFlashAttribute("error", "El usuario seleccionado como paciente no es PACIENTE.");
            return "redirect:/admin-hospital/assignments/new";
        }

        if (doctor.getPatients().contains(patient)) {
            ra.addFlashAttribute("error", "Ese paciente ya está asignado a ese médico.");
            return "redirect:/admin-hospital/assignments/new";
        }

        doctor.getPatients().add(patient);
        userRepository.save(doctor);

        ra.addFlashAttribute("success", "Asignación creada correctamente.");
        return "redirect:/admin-hospital/assignments/new";
    }

}
