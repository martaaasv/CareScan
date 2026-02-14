error id: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/AdminHospitalController.java:_empty_/RedirectAttributes#addFlashAttribute#
file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/AdminHospitalController.java
empty definition using pc, found symbol in pc: _empty_/RedirectAttributes#addFlashAttribute#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 10211
uri: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/AdminHospitalController.java
text:
```scala
package es.marta.tfg.carescan.controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import es.marta.tfg.carescan.model.DoctorPatientAssignment;
import es.marta.tfg.carescan.model.Estado;
import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.DoctorPatientAssignmentRepository;
import es.marta.tfg.carescan.repository.UserRepository;

@Controller
@RequestMapping("/admin-hospital")
public class AdminHospitalController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DoctorPatientAssignmentRepository assignmentRepository;

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
        patient.setEstado(Estado.ESPERANDO_ASIGNACION);

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

        if (!patient.isActive()) {
            ra.addFlashAttribute("error", "No se puede asignar médico a un paciente INACTIVO.");
            return "redirect:/admin-hospital/assignments/new";
        }

        // Si ya existe asignación activa con ese mismo médico, no hacer nada
        if (assignmentRepository.existsByDoctorAndPatientAndActiveTrue(doctor, patient)) {
            ra.addFlashAttribute("success", "Ese paciente ya está asignado a ese médico.");
            return "redirect:/admin-hospital/assignments/new";
        }

        // Cerrar asignación activa anterior del paciente (si existe)
        Optional<DoctorPatientAssignment> activeAssignmentOpt = assignmentRepository.findByPatientAndActiveTrue(patient);
        if (activeAssignmentOpt.isPresent()) {
            DoctorPatientAssignment a = activeAssignmentOpt.get();
            a.setActive(false);
            a.setEndDate(LocalDateTime.now());
            assignmentRepository.save(a);
        }

        // Crear nueva asignación
        DoctorPatientAssignment newAssign = new DoctorPatientAssignment();
        newAssign.setDoctor(doctor);
        newAssign.setPatient(patient);
        newAssign.setStartDate(LocalDateTime.now());
        newAssign.setActive(true);

        assignmentRepository.save(newAssign);

        ra.addFlashAttribute("success", "Asignación creada correctamente.");
        return "redirect:/admin-hospital/assignments/new";
    }

    // PARA LA TABA DE ADMIN H
    @GetMapping("/patients")
    public String patientManagement(Model model, Authentication auth) {

        if (auth != null) {
            userRepository.findByEmail(auth.getName()).ifPresent(admin -> {
                model.addAttribute("userId", admin.getId());
                model.addAttribute("username", admin.getName());
            });
        }

        List<User> patients = userRepository.findAllByRole(Role.PACIENTE);
        List<DoctorPatientAssignment> activeAssignments = assignmentRepository.findByPatientInAndActiveTrue(patients);

        Map<Long, String> patientDoctorMap = new HashMap<>();
        for (DoctorPatientAssignment a : activeAssignments) {
            patientDoctorMap.put(a.getPatient().getId(), a.getDoctor().getName());
        }

        model.addAttribute("patients", patients);
        model.addAttribute("patientDoctorMap", patientDoctorMap);

        return "adminHospital/patientManagement";
    }

    @PostMapping("/patients/{patientId}/deactivate")
    public String deactivatePatient(@PathVariable Long patientId, RedirectAttributes ra) {

        User patient = userRepository.findById(patientId).orElse(null);
        if (patient == null || patient.getRole() != Role.PACIENTE) {
            ra.addFlashAttribute("error", "Paciente no encontrado.");
            return "redirect:/admin-hospital/patients";
        }

        patient.setActive(false);
        userRepository.save(patient);

        Optional<DoctorPatientAssignment> activeAssignmentOpt = assignmentRepository.findByPatientAndActiveTrue(patient);
        if (activeAssignmentOpt.isPresent()) {
            DoctorPatientAssignment a = activeAssignmentOpt.get();
            a.setActive(false);
            a.setEndDate(LocalDateTime.now());
            assignmentRepository.save(a);
        }

        ra.addFlashAttribute("success", "Paciente dado de baja correctamente.");
        return "redirect:/admin-hospital/patients";
    }

    @GetMapping("/patients/{patientId}/changeDoctor")
    public String changeDoctorForm(@PathVariable Long patientId, Model model, Authentication auth) {

        User patient = userRepository.findById(patientId).orElse(null);
        if (patient == null || patient.getRole() != Role.PACIENTE) {
            return "redirect:/admin-hospital/patients";
        }

        List<User> doctors = userRepository.findAllByRole(Role.MEDICO);

        model.addAttribute("patient", patient);
        model.addAttribute("doctors", doctors);

        if (auth != null) {
            userRepository.findByEmail(auth.getName()).ifPresent(admin -> {
                model.addAttribute("userId", admin.getId());
                model.addAttribute("username", admin.getName());
            });
        }

        return "adminHospital/changeDoctor";
    }

    @PostMapping("/patients/{patientId}/changeDoctor")
    public String changeDoctor(@PathVariable Long patientId,
            @RequestParam("doctorId") Long doctorId,
            RedirectAttributes ra) {

        User patient = userRepository.findById(patientId).orElse(null);
        User doctor = userRepository.findById(doctorId).orElse(null);

        if (patient == null || patient.getRole() != Role.PACIENTE) {
            ra.addFlashAttribute("error", "Paciente no encontrado.");
            return "redirect:/admin-hospital/patients";
        }

        if (patient.getEstado() == Estado.INACTIVO) {
            ra.addF@@lashAttribute("error", "No se puede asignar médico a un paciente INACTIVO.");
            return "redirect:/admin-hospital/assignments/new";
        }

        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            ra.addFlashAttribute("error", "Médico no encontrado.");
            return "redirect:/admin-hospital/patients/" + patientId + "/changeDoctor";
        }

        Optional<DoctorPatientAssignment> activeAssignmentOpt = assignmentRepository.findByPatientAndActiveTrue(patient);
        if (activeAssignmentOpt.isPresent()) {
            DoctorPatientAssignment a = activeAssignmentOpt.get();
            a.setActive(false);
            a.setEndDate(LocalDateTime.now());
            assignmentRepository.save(a);
        }

        // Crear nueva asignación
        DoctorPatientAssignment newAssign = new DoctorPatientAssignment();
        newAssign.setDoctor(doctor);
        newAssign.setPatient(patient);
        newAssign.setStartDate(LocalDateTime.now());
        newAssign.setActive(true);

        assignmentRepository.save(newAssign);

        ra.addFlashAttribute("success", "Médico asignado correctamente.");
        return "redirect:/admin-hospital/patients";
    }

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/RedirectAttributes#addFlashAttribute#