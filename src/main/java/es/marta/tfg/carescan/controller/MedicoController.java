package es.marta.tfg.carescan.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
import es.marta.tfg.carescan.service.BrainTumorAnalysisService;
import es.marta.tfg.carescan.service.BrainTumorPrediction;

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

    @Autowired
    private BrainTumorAnalysisService brainTumorAnalysisService;

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
        LinkedHashMap<Long, User> patientsById = new LinkedHashMap<>();

        assignments.stream()
                .map(DoctorPatientAssignment::getPatient)
                .forEach(patient -> patientsById.put(patient.getId(), patient));

        consultaRepository.findByUserOrderByFechaHoraDesc(doctor).stream()
                .map(Consulta::getPatient)
                .filter(patient -> patient != null && patient.getEstado() == Estado.INACTIVO)
                .forEach(patient -> patientsById.putIfAbsent(patient.getId(), patient));

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("patients", List.copyOf(patientsById.values()));

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

        if (!canInteractWithPatient(doctor, patient)) {
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

        if (!canInteractWithPatient(doctor, patient)) {
            return "redirect:/access-denied";
        }

        if (file == null || file.isEmpty()) {
            return "redirect:/medico/patients/" + patientId + "/upload";
        }

        Consulta consulta = new Consulta();
        consulta.setUser(doctor);
        consulta.setPatient(patient);
        consulta.setFechaHora(LocalDateTime.now());
        consulta.setNombreArchivo(file.getOriginalFilename());
        consulta.setContentType(file.getContentType());
        consulta.setImagen(file.getBytes());
        Consulta saved = consultaRepository.save(consulta);

        analisisIARepository.save(createOrUpdateAnalysis(saved, saved.getAnalisisIA()));

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

        if (!canInteractWithPatient(doctor, consulta.getPatient())) {
            return "redirect:/access-denied";
        }

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("consulta", consulta);
        model.addAttribute("patient", consulta.getPatient());

        return "medico/caseResults";
    }

    @PostMapping("/consultas/{consultaId}/publish")
    public String publishResult(@PathVariable Long consultaId,
            @RequestParam(value = "comentarioMedico", required = false) String comentarioMedico,
            @RequestParam(value = "redirectTo", required = false) String redirectTo,
            Authentication auth) {

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

        if (!canInteractWithPatient(doctor, consulta.getPatient())) {
            return "redirect:/access-denied";
        }

        AnalisisIA analisis = consulta.getAnalisisIA();
        if (analisis != null) {
            analisis.setComentarioMedico(comentarioMedico != null ? comentarioMedico.trim() : null);
            analisis.setVisiblePaciente(true);
            analisisIARepository.save(analisis);
        }

        if (redirectTo != null && redirectTo.startsWith("/medico/")) {
            return "redirect:" + redirectTo;
        }

        return "redirect:/medico/consultas/" + consultaId + "/results";
    }

    private User requireDoctor(Authentication auth) {
        if (auth == null) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private boolean canInteractWithPatient(User doctor, User patient) {
        return doctor != null
                && doctor.getRole() == Role.MEDICO
                && patient != null
                && patient.getRole() == Role.PACIENTE
                && !patient.isBlocked()
                && patient.getEstado() != Estado.INACTIVO
                && assignmentRepository.existsByDoctorAndPatientAndActiveTrue(doctor, patient);
    }

    private boolean hasHistoryAccess(User doctor, User patient) {
        return doctor != null
                && doctor.getRole() == Role.MEDICO
                && patient != null
                && patient.getRole() == Role.PACIENTE
                && (assignmentRepository.existsByDoctorAndPatientAndActiveTrue(doctor, patient)
                        || ((patient.getEstado() == Estado.INACTIVO || patient.isBlocked())
                                && consultaRepository.existsByUserAndPatient(doctor, patient)));
    }

    @GetMapping("/patients/{patientId}/radiografias")
    public String patientRadiographs(@PathVariable Long patientId, Model model, Authentication auth) {
        User doctor = requireDoctor(auth);
        if (doctor == null) {
            return "redirect:/login";
        }

        User patient = userRepository.findById(patientId).orElse(null);
        if (!hasHistoryAccess(doctor, patient)) {
            return "redirect:/access-denied";
        }

        List<Consulta> consultas = consultaRepository.findByUserAndPatientOrderByFechaHoraDesc(doctor, patient);

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("patient", patient);
        model.addAttribute("consultas", consultas);
        model.addAttribute("canInteractWithPatient", canInteractWithPatient(doctor, patient));

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
        if (!hasHistoryAccess(doctor, patient)) {
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
        model.addAttribute("analisis", consulta.getAnalisisIA());
        model.addAttribute("canInteractWithPatient", canInteractWithPatient(doctor, patient));

        return "medico/radiographyDetail";
    }

    @GetMapping("/patients/{patientId}/radiografias/{consultaId}/edit")
    public String editRadiographyForm(
            @PathVariable Long patientId,
            @PathVariable Long consultaId,
            Model model,
            Authentication auth) {

        User doctor = requireDoctor(auth);
        if (doctor == null) {
            return "redirect:/login";
        }

        User patient = userRepository.findById(patientId).orElse(null);
        if (!canInteractWithPatient(doctor, patient)) {
            return "redirect:/access-denied";
        }

        Consulta consulta = consultaRepository.findByIdAndUserAndPatient(consultaId, doctor, patient).orElse(null);
        if (consulta == null) {
            return "redirect:/access-denied";
        }

        unpublishForEdition(consulta);

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("patient", patient);
        model.addAttribute("consulta", consulta);
        model.addAttribute("analisis", consulta.getAnalisisIA());

        return "medico/editRadiography";
    }

    @PostMapping("/patients/{patientId}/radiografias/{consultaId}/edit")
    public String editRadiography(
            @PathVariable Long patientId,
            @PathVariable Long consultaId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "comentarioMedico", required = false) String comentarioMedico,
            Authentication auth) throws IOException {

        User doctor = requireDoctor(auth);
        if (doctor == null) {
            return "redirect:/login";
        }

        User patient = userRepository.findById(patientId).orElse(null);
        if (!canInteractWithPatient(doctor, patient)) {
            return "redirect:/access-denied";
        }

        Consulta consulta = consultaRepository.findByIdAndUserAndPatient(consultaId, doctor, patient).orElse(null);
        if (consulta == null) {
            return "redirect:/access-denied";
        }

        Consulta saved = consulta;
        AnalisisIA analisis = consulta.getAnalisisIA();

        if (file != null && !file.isEmpty()) {
            consulta.setNombreArchivo(file.getOriginalFilename());
            consulta.setContentType(file.getContentType());
            consulta.setImagen(file.getBytes());
            saved = consultaRepository.save(consulta);
            analisis = createOrUpdateAnalysis(saved, saved.getAnalisisIA());
        } else if (analisis == null) {
            analisis = createOrUpdateAnalysis(saved, null);
        }

        analisis.setComentarioMedico(normalizeComment(comentarioMedico));
        analisis.setVisiblePaciente(false);
        analisisIARepository.save(analisis);

        return "redirect:/medico/patients/" + patientId + "/radiografias/" + consultaId;
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
        if (!hasHistoryAccess(doctor, patient)) {
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

    @GetMapping("/consultas/{consultaId}/heatmap")
    @ResponseBody
    public ResponseEntity<byte[]> consultaHeatmap(@PathVariable Long consultaId, Authentication auth) {
        User doctor = requireDoctor(auth);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return ResponseEntity.status(403).build();
        }

        Consulta consulta = consultaRepository.findById(consultaId).orElse(null);
        if (consulta == null) {
            return ResponseEntity.notFound().build();
        }

        User patient = consulta.getPatient();
        if (!hasHistoryAccess(doctor, patient)) {
            return ResponseEntity.status(403).build();
        }

        AnalisisIA analisis = consulta.getAnalisisIA();
        if (analisis == null || analisis.getHeatmapImagen() == null || analisis.getHeatmapImagen().length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType;
        try {
            mediaType = (analisis.getHeatmapContentType() != null)
                    ? MediaType.parseMediaType(analisis.getHeatmapContentType())
                    : MediaType.IMAGE_PNG;
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok().contentType(mediaType).body(analisis.getHeatmapImagen());
    }

    private AnalisisIA createOrUpdateAnalysis(Consulta consulta, AnalisisIA existingAnalysis) {
        BrainTumorPrediction prediction = brainTumorAnalysisService.predict(consulta.getImagen());

        AnalisisIA analisis = existingAnalysis != null ? existingAnalysis : new AnalisisIA();
        analisis.setConsulta(consulta);
        analisis.setProbabilidad(prediction.confidence());
        analisis.setEtiqueta(prediction.label());
        analisis.setModeloVersion(prediction.modelVersion());
        analisis.setHeatmapImagen(prediction.heatmapImage());
        analisis.setHeatmapContentType(prediction.heatmapContentType());
        analisis.setEjecutadoEn(LocalDateTime.now());
        analisis.setVisiblePaciente(false);

        return analisis;
    }

    private void unpublishForEdition(Consulta consulta) {
        AnalisisIA analisis = consulta.getAnalisisIA();
        if (analisis != null && analisis.isVisiblePaciente()) {
            analisis.setVisiblePaciente(false);
            analisisIARepository.save(analisis);
        }
    }

    private String normalizeComment(String comentarioMedico) {
        if (comentarioMedico == null || comentarioMedico.trim().isEmpty()) {
            return null;
        }
        return comentarioMedico.trim();
    }

}
