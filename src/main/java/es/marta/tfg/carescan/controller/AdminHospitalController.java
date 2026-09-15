package es.marta.tfg.carescan.controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.Estado;
import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.DoctorPatientAssignmentRepository;
import es.marta.tfg.carescan.repository.ConsultaRepository;
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

    @Autowired
    private ConsultaRepository consultaRepository;


    private boolean isSelectablePatient(User u) {
        return u != null
                && u.getRole() == Role.PACIENTE
                && u.getEstado() != Estado.INACTIVO
                && !u.isBlocked();
    }

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
            @RequestParam("role") String roleName,
            RedirectAttributes ra) {

        if (userRepository.findByEmail(email).isPresent()) {
            ra.addFlashAttribute("error", "Ya existe un usuario con ese email.");
            return "redirect:/admin-hospital/patients/new";
        }

        Role role;
        try {
            role = Role.valueOf(roleName);
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", "Rol no válido.");
            return "redirect:/admin-hospital/patients/new";
        }

        if (role != Role.PACIENTE && role != Role.MEDICO) {
            ra.addFlashAttribute("error", "Solo se pueden crear usuarios con rol PACIENTE o MEDICO.");
            return "redirect:/admin-hospital/patients/new";
        }

        String tempPassword = generateTempPassword(10);

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setTemporalPassword(true);
        user.setActive(true);

        if (role == Role.PACIENTE) {
            user.setEstado(Estado.ESPERANDO_ASIGNACION);
        } else {
            user.setEstado(null);
        }

        userRepository.save(user);

        ra.addFlashAttribute("success", "Usuario creado correctamente.");
        ra.addFlashAttribute("tempPassword", tempPassword);
        ra.addFlashAttribute("createdEmail", email);
        ra.addFlashAttribute("createdRole", role.name());

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

    // --------------------------
    // FORM ASIGNAR MEDICO A PACIENTE
    // (aquí el desplegable: NO INACTIVOS)
    // --------------------------
    @GetMapping("/assignments/new")
    public String newAssignmentForm(Model model, Authentication auth) {

        List<User> doctors = userRepository.findAllByRole(Role.MEDICO)
                .stream()
                .filter(User::isActive)
                .toList();

        // SOLO pacientes NO INACTIVOS (para el desplegable)
        List<User> patients = userRepository.findAllByRole(Role.PACIENTE)
                .stream()
                .filter(this::isSelectablePatient)
                .toList();

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

        if (!doctor.isActive()) {
            ra.addFlashAttribute("error", "No se puede asignar pacientes a un medico INACTIVO.");
            return "redirect:/admin-hospital/assignments/new";
        }

        if (patient.getRole() != Role.PACIENTE) {
            ra.addFlashAttribute("error", "El usuario seleccionado como paciente no es PACIENTE.");
            return "redirect:/admin-hospital/assignments/new";
        }

        // BLOQUEO REAL: paciente inactivo no se asigna nunca
        if (patient.getEstado() == Estado.INACTIVO) {
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

        // si se asigna, pasa a activo
        patient.setEstado(Estado.ACTIVO);
        userRepository.save(patient);

        assignmentRepository.save(newAssign);

        ra.addFlashAttribute("success", "Asignación creada correctamente.");
        return "redirect:/admin-hospital/assignments/new";
    }

    // --------------------------
    // TABLA GESTIÓN PACIENTES
    // --------------------------
    @GetMapping("/patients")
    public String patientManagement(Model model, Authentication auth) {

        if (auth != null) {
            userRepository.findByEmail(auth.getName()).ifPresent(admin -> {
                model.addAttribute("userId", admin.getId());
                model.addAttribute("username", admin.getName());
            });
        }

        List<User> patients = userRepository.findAllByRole(Role.PACIENTE);
        List<User> doctors = userRepository.findAllByRole(Role.MEDICO);

        List<DoctorPatientAssignment> activeAssignments = assignmentRepository.findByPatientInAndActiveTrue(patients);

        Map<Long, String> patientDoctorMap = new HashMap<>();
        for (DoctorPatientAssignment a : activeAssignments) {
            patientDoctorMap.put(a.getPatient().getId(), a.getDoctor().getName());
        }

        model.addAttribute("patients", patients);
        model.addAttribute("doctors", doctors);
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

        // Si ya está inactivo, no repetir acción
        if (patient.getEstado() == Estado.INACTIVO) {
            ra.addFlashAttribute("error", "El paciente ya está INACTIVO.");
            return "redirect:/admin-hospital/patients";
        }

        patient.setEstado(Estado.INACTIVO);
        userRepository.save(patient);

        // cerrar asignación activa si existe
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

    @GetMapping("/patients/{patientId}/reactivate")
    public String reactivatePatientForm(@PathVariable Long patientId, Model model, Authentication auth,
            RedirectAttributes ra) {

        User patient = userRepository.findById(patientId).orElse(null);

        if (patient == null || patient.getRole() != Role.PACIENTE) {
            ra.addFlashAttribute("error", "Paciente no encontrado.");
            return "redirect:/admin-hospital/patients";
        }

        if (patient.getEstado() != Estado.INACTIVO) {
            ra.addFlashAttribute("error", "Solo se puede reactivar un paciente INACTIVO.");
            return "redirect:/admin-hospital/patients";
        }

        List<User> doctors = userRepository.findAllByRole(Role.MEDICO)
                .stream()
                .filter(User::isActive)
                .toList();

        model.addAttribute("patient", patient);
        model.addAttribute("doctors", doctors);

        if (auth != null) {
            userRepository.findByEmail(auth.getName()).ifPresent(admin -> {
                model.addAttribute("userId", admin.getId());
                model.addAttribute("username", admin.getName());
            });
        }

        return "adminHospital/reactivatePatient";
    }

    @PostMapping("/patients/{patientId}/reactivate")
    public String reactivatePatient(@PathVariable Long patientId,
            @RequestParam("doctorId") Long doctorId,
            RedirectAttributes ra) {

        User patient = userRepository.findById(patientId).orElse(null);

        if (patient == null || patient.getRole() != Role.PACIENTE) {
            ra.addFlashAttribute("error", "Paciente no encontrado.");
            return "redirect:/admin-hospital/patients";
        }

        if (patient.getEstado() != Estado.INACTIVO) {
            ra.addFlashAttribute("error", "Solo se puede reactivar un paciente INACTIVO.");
            return "redirect:/admin-hospital/patients";
        }

        User doctor = userRepository.findById(doctorId).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            ra.addFlashAttribute("error", "Medico no encontrado.");
            return "redirect:/admin-hospital/patients/" + patientId + "/reactivate";
        }

        if (!doctor.isActive()) {
            ra.addFlashAttribute("error", "No se puede asignar pacientes a un medico INACTIVO.");
            return "redirect:/admin-hospital/patients/" + patientId + "/reactivate";
        }

        Optional<DoctorPatientAssignment> activeAssignmentOpt = assignmentRepository.findByPatientAndActiveTrue(patient);
        if (activeAssignmentOpt.isPresent()) {
            DoctorPatientAssignment activeAssignment = activeAssignmentOpt.get();
            activeAssignment.setActive(false);
            activeAssignment.setEndDate(LocalDateTime.now());
            assignmentRepository.save(activeAssignment);
        }

        DoctorPatientAssignment newAssign = new DoctorPatientAssignment();
        newAssign.setDoctor(doctor);
        newAssign.setPatient(patient);
        newAssign.setStartDate(LocalDateTime.now());
        newAssign.setActive(true);
        assignmentRepository.save(newAssign);

        patient.setEstado(Estado.ACTIVO);
        userRepository.save(patient);

        ra.addFlashAttribute("success", "Paciente reactivado correctamente.");
        return "redirect:/admin-hospital/patients";
    }

    @GetMapping("/patients/{patientId}/changeDoctor")
    public String changeDoctorForm(@PathVariable Long patientId, Model model, Authentication auth, RedirectAttributes ra) {

        User patient = userRepository.findById(patientId).orElse(null);

        if (patient == null || patient.getRole() != Role.PACIENTE) {
            ra.addFlashAttribute("error", "Paciente no encontrado.");
            return "redirect:/admin-hospital/patients";
        }

        // BLOQUEO REAL: si está inactivo, no se puede cambiar médico
        if (patient.getEstado() == Estado.INACTIVO) {
            ra.addFlashAttribute("error", "No se puede cambiar el médico de un paciente INACTIVO.");
            return "redirect:/admin-hospital/patients";
        }

        List<User> doctors = userRepository.findAllByRole(Role.MEDICO)
                .stream()
                .filter(User::isActive)
                .toList();

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

        if (patient == null || patient.getRole() != Role.PACIENTE) {
            ra.addFlashAttribute("error", "Paciente no encontrado.");
            return "redirect:/admin-hospital/patients";
        }

        // BLOQUEO REAL: si está inactivo, no se cambia médico
        if (patient.getEstado() == Estado.INACTIVO) {
            ra.addFlashAttribute("error", "No se puede cambiar el médico de un paciente INACTIVO.");
            return "redirect:/admin-hospital/patients";
        }

        User doctor = userRepository.findById(doctorId).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            ra.addFlashAttribute("error", "Médico no encontrado.");
            return "redirect:/admin-hospital/patients/" + patientId + "/changeDoctor";
        }

        if (!doctor.isActive()) {
            ra.addFlashAttribute("error", "No se puede asignar pacientes a un medico INACTIVO.");
            return "redirect:/admin-hospital/patients/" + patientId + "/changeDoctor";
        }

        Optional<DoctorPatientAssignment> activeAssignmentOpt = assignmentRepository.findByPatientAndActiveTrue(patient);
        if (activeAssignmentOpt.isPresent()) {
            DoctorPatientAssignment a = activeAssignmentOpt.get();
            a.setActive(false);
            a.setEndDate(LocalDateTime.now());
            assignmentRepository.save(a);
        }

        DoctorPatientAssignment newAssign = new DoctorPatientAssignment();
        newAssign.setDoctor(doctor);
        newAssign.setPatient(patient);
        newAssign.setStartDate(LocalDateTime.now());
        newAssign.setActive(true);
        assignmentRepository.save(newAssign);
        patient.setEstado(Estado.ACTIVO);
        userRepository.save(patient);

        ra.addFlashAttribute("success", "Médico asignado correctamente.");
        return "redirect:/admin-hospital/patients";
    }

    @PostMapping("/doctors/{doctorId}/deactivate")
    public String deactivateDoctor(@PathVariable Long doctorId, RedirectAttributes ra) {

        User doctor = userRepository.findById(doctorId).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            ra.addFlashAttribute("error", "Medico no encontrado.");
            return "redirect:/admin-hospital/patients?tab=doctors";
        }

        if (!doctor.isActive()) {
            ra.addFlashAttribute("error", "El medico ya esta INACTIVO.");
            return "redirect:/admin-hospital/patients?tab=doctors";
        }

        List<User> availableDoctors = userRepository.findAllByRole(Role.MEDICO)
                .stream()
                .filter(User::isActive)
                .filter(d -> !d.getId().equals(doctor.getId()))
                .toList();

        List<DoctorPatientAssignment> currentAssignments = assignmentRepository.findByDoctorAndActiveTrue(doctor)
                .stream()
                .filter(a -> a.getPatient() != null && a.getPatient().getEstado() != Estado.INACTIVO)
                .toList();

        if (!currentAssignments.isEmpty() && availableDoctors.isEmpty()) {
            ra.addFlashAttribute("error",
                    "No se puede dar de baja al medico porque no hay otros medicos activos para reasignar sus pacientes.");
            return "redirect:/admin-hospital/patients?tab=doctors";
        }

        List<DoctorPatientAssignment> assignmentsToSave = new ArrayList<>();
        List<Consulta> consultasToSave = new ArrayList<>();
        int doctorIndex = 0;

        for (DoctorPatientAssignment assignment : currentAssignments) {
            User newDoctor = availableDoctors.get(doctorIndex % availableDoctors.size());
            User patient = assignment.getPatient();

            assignment.setActive(false);
            assignment.setEndDate(LocalDateTime.now());
            assignmentsToSave.add(assignment);

            DoctorPatientAssignment newAssignment = new DoctorPatientAssignment();
            newAssignment.setDoctor(newDoctor);
            newAssignment.setPatient(patient);
            newAssignment.setStartDate(LocalDateTime.now());
            newAssignment.setActive(true);
            assignmentsToSave.add(newAssignment);

            List<Consulta> consultas = consultaRepository.findByUserAndPatientOrderByFechaHoraDesc(doctor, patient);
            for (Consulta consulta : consultas) {
                consulta.setUser(newDoctor);
                consultasToSave.add(consulta);
            }

            doctorIndex++;
        }

        assignmentRepository.saveAll(assignmentsToSave);
        consultaRepository.saveAll(consultasToSave);

        doctor.setActive(false);
        userRepository.save(doctor);

        ra.addFlashAttribute("success",
                "Medico dado de baja correctamente. Sus pacientes se han redistribuido entre los medicos activos.");
        return "redirect:/admin-hospital/patients?tab=doctors";
    }

    @PostMapping("/doctors/{doctorId}/reactivate")
    public String reactivateDoctor(@PathVariable Long doctorId, RedirectAttributes ra) {

        User doctor = userRepository.findById(doctorId).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            ra.addFlashAttribute("error", "Medico no encontrado.");
            return "redirect:/admin-hospital/patients?tab=doctors";
        }

        if (doctor.isActive()) {
            ra.addFlashAttribute("error", "El medico ya esta ACTIVO.");
            return "redirect:/admin-hospital/patients?tab=doctors";
        }

        doctor.setActive(true);
        userRepository.save(doctor);

        ra.addFlashAttribute("success", "Medico reactivado correctamente. No se le han asignado pacientes automaticamente.");
        return "redirect:/admin-hospital/patients?tab=doctors";
    }

}
