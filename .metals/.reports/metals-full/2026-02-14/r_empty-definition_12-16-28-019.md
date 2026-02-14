error id: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/MedicoController.java:_empty_/DoctorPatientAssignmentRepository#existsByDoctorAndPatientAndActiveTrue#
file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/MedicoController.java
empty definition using pc, found symbol in pc: _empty_/DoctorPatientAssignmentRepository#existsByDoctorAndPatientAndActiveTrue#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 3330
uri: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/MedicoController.java
text:
```scala
package es.marta.tfg.carescan.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.marta.tfg.carescan.model.AnalisisIA;
import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.DoctorPatientAssignment;
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

        if (!assignmentRepository.existsByDoctorAndPat@@ientAndActiveTrue(doctor, patient)) {
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

        if (!doctor.getPatients().contains(patient)) {
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

        double prob = Math.random();
        String etiqueta = prob < 0.33 ? "NEGATIVO" : (prob < 0.66 ? "SOSPECHOSO" : "POSITIVO");

        AnalisisIA analisis = new AnalisisIA();
        analisis.setConsulta(saved);
        analisis.setProbabilidad(prob);
        analisis.setEtiqueta(etiqueta);
        analisis.setModeloVersion("stub-v1");
        analisis.setEjecutadoEn(LocalDateTime.now());

        analisisIARepository.save(analisis);

        return "redirect:/medico/consultas/" + saved.getId() + "/results";

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

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/DoctorPatientAssignmentRepository#existsByDoctorAndPatientAndActiveTrue#