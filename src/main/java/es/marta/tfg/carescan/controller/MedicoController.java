package es.marta.tfg.carescan.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import es.marta.tfg.carescan.model.AnalisisIA;
import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.DoctorPatientAssignment;
import es.marta.tfg.carescan.model.Estado;
import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.AnalisisIARepository;
import es.marta.tfg.carescan.repository.ConsultaRepository;
import es.marta.tfg.carescan.repository.DoctorPatientAssignmentRepository;
import es.marta.tfg.carescan.repository.UserRepository;

@Controller
@RequestMapping("/medico")
public class MedicoController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private AnalisisIARepository analisisIARepository;

    @Autowired
    private DoctorPatientAssignmentRepository assignmentRepository;

    @GetMapping("/patients")
    public String patientList(Model model, Authentication auth) {

        if (auth == null) {
            return "redirect:/login";
        }

        User doctor = userRepository.findByEmail(auth.getName()).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return "redirect:/access-denied";
        }

        List<DoctorPatientAssignment> assignments = assignmentRepository.findByDoctorAndActiveTrue(doctor);

        List<User> patients = assignments.stream()
                .map(DoctorPatientAssignment::getPatient)
                .toList();

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("patients", patients);

        return "medico/patientList";
    }

    // Subir la radiografía de un paciente 
    @GetMapping("/patients/{patientId}/upload")
    public String uploadRadiographyForm(@PathVariable Long patientId, Model model, Authentication auth) {

        if (auth == null) {
            return "redirect:/login";
        }

        User doctor = userRepository.findByEmail(auth.getName()).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return "redirect:/access-denied";
        }

        User patient = userRepository.findById(patientId).orElse(null);
        if (patient == null || patient.getRole() != Role.PACIENTE) {
            return "redirect:/access-denied";
        }

        if (patient.getEstado() == Estado.INACTIVO) {
            return "redirect:/access-denied";
        }

        if (!assignmentRepository.existsByDoctorAndPatientAndActiveTrue(doctor, patient)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("patient", patient);

        return "medico/uploadRadiography";
    }

    @PostMapping("/patients/{patientId}/upload")
    public String uploadRadiography(@PathVariable Long patientId,
            Authentication auth,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (auth == null) {
            return "redirect:/login";
        }

        User doctor = userRepository.findByEmail(auth.getName()).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return "redirect:/access-denied";
        }

        User patient = userRepository.findById(patientId).orElse(null);
        if (patient == null || patient.getRole() != Role.PACIENTE) {
            return "redirect:/access-denied";
        }

        if (!assignmentRepository.existsByDoctorAndPatientAndActiveTrue(doctor, patient)) {
            return "redirect:/access-denied";
        }

        if (file == null || file.isEmpty()) {
            return "redirect:/medico/patients/" + patientId + "/upload";
        }

        Consulta consulta = new Consulta();
        consulta.setUser(doctor);
        consulta.setPatient(patient);
        consulta.setFechaHora(LocalDateTime.now());
        consulta.setContentType(file.getContentType());
        consulta.setImagen(file.getBytes());
        Consulta saved = consultaRepository.save(consulta);

        double aux = Math.random();
        double prob = Math.round(aux * 100.0) / 100.0;
        String etiqueta = prob < 0.33 ? "NEGATIVO" : (prob < 0.66 ? "SOSPECHOSO" : "POSITIVO");

        AnalisisIA analisis = new AnalisisIA();
        analisis.setConsulta(saved);
        analisis.setProbabilidad(prob);
        analisis.setEtiqueta(etiqueta);
        analisis.setModeloVersion("stub-v1");
        analisis.setEjecutadoEn(LocalDateTime.now());

        analisisIARepository.save(analisis);

        return "redirect:/medico/patients/" + patientId + "/radiografias/" + saved.getId();

    }

    @GetMapping("/consultas/{consultaId}/results")
    public String consultaResults(@PathVariable Long consultaId, Model model, Authentication auth) {

        if (auth == null) {
            return "redirect:/login";
        }

        User doctor = userRepository.findByEmail(auth.getName()).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return "redirect:/access-denied";
        }

        Consulta consulta = consultaRepository.findById(consultaId).orElse(null);
        if (consulta == null) {
            return "redirect:/access-denied";
        }

        if (!consulta.getUser().getId().equals(doctor.getId())) {
            return "redirect:/access-denied";
        }

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("consulta", consulta);
        model.addAttribute("patient", consulta.getPatient());

        return "medico/caseResults";
    }

    @PostMapping("/consultas/{consultaId}/publish")
    public String publishResult(@PathVariable Long consultaId, Authentication auth) {

        if (auth == null) {
            return "redirect:/login";
        }

        User doctor = userRepository.findByEmail(auth.getName()).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return "redirect:/access-denied";
        }

        Consulta consulta = consultaRepository.findById(consultaId).orElse(null);
        if (consulta == null) {
            return "redirect:/access-denied";
        }

        if (!consulta.getUser().getId().equals(doctor.getId())) {
            return "redirect:/access-denied";
        }

        AnalisisIA analisis = consulta.getAnalisisIA();
        if (analisis != null) {
            analisis.setVisiblePaciente(true);
            analisisIARepository.save(analisis);
        }

        return "redirect:/medico/consultas/" + consultaId + "/results";
    }

    private User requireDoctor(Authentication auth) {
        if (auth == null) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private boolean hasAccess(User doctor, User patient) {
        return doctor != null
                && doctor.getRole() == Role.MEDICO
                && patient != null
                && patient.getRole() == Role.PACIENTE
                && patient.getEstado() != Estado.INACTIVO
                && assignmentRepository.existsByDoctorAndPatientAndActiveTrue(doctor, patient);
    }

    @GetMapping("/patients/{patientId}/radiografias")
    public String patientRadiographs(@PathVariable Long patientId, Model model, Authentication auth) {
        User doctor = requireDoctor(auth);
        if (doctor == null) {
            return "redirect:/login";
        }

        User patient = userRepository.findById(patientId).orElse(null);
        if (!hasAccess(doctor, patient)) {
            return "redirect:/access-denied";
        }

        List<Consulta> consultas = consultaRepository.findByUserAndPatientOrderByFechaHoraDesc(doctor, patient);

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("patient", patient);
        model.addAttribute("consultas", consultas);

        return "medico/patientRadiographs";
    }

    @GetMapping("/patients/{patientId}/radiografias/{consultaId}")
    public String radiographyDetail(
            @PathVariable Long patientId,
            @PathVariable Long consultaId,
            Model model,
            Authentication auth) {

        User doctor = requireDoctor(auth);
        if (doctor == null) {
            return "redirect:/login";
        }

        User patient = userRepository.findById(patientId).orElse(null);
        if (!hasAccess(doctor, patient)) {
            return "redirect:/access-denied";
        }

        Consulta consulta = consultaRepository.findByIdAndUserAndPatient(consultaId, doctor, patient).orElse(null);
        if (consulta == null) {
            return "redirect:/access-denied";
        }

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("patient", patient);
        model.addAttribute("consulta", consulta);
        model.addAttribute("analisis", consulta.getAnalisisIA()); // si lo tienes mapeado

        return "medico/radiographyDetail";
    }

    @GetMapping("/consultas/{consultaId}/image")
    @ResponseBody
    public ResponseEntity<byte[]> consultaImage(@PathVariable Long consultaId, Authentication auth) {
        User doctor = requireDoctor(auth);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return ResponseEntity.status(403).build();
        }

        Consulta consulta = consultaRepository.findById(consultaId).orElse(null);
        if (consulta == null) {
            return ResponseEntity.notFound().build();
        }

        User patient = consulta.getPatient();
        if (!hasAccess(doctor, patient)) {
            return ResponseEntity.status(403).build();
        }

        byte[] img = consulta.getImagen();
        if (img == null || img.length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType;
        try {
            mediaType = (consulta.getContentType() != null)
                    ? MediaType.parseMediaType(consulta.getContentType())
                    : MediaType.IMAGE_JPEG;
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok().contentType(mediaType).body(img);
    }

}
