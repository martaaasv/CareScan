package es.marta.tfg.carescan.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.ConsultaRepository;
import es.marta.tfg.carescan.repository.UserRepository;

@Controller
@RequestMapping("/paciente")
public class PacienteController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @GetMapping("/results")
    public String patientResults(Model model, Authentication auth) {

        if (auth == null) return "redirect:/login";

        User patient = userRepository.findByEmail(auth.getName()).orElse(null);
        if (patient == null || patient.getRole() != Role.PACIENTE) {
            return "redirect:/access-denied";
        }

        List<Consulta> consultas = consultaRepository.findByPatientOrderByFechaHoraDesc(patient)
                .stream()
                .filter(c -> c.getAnalisisIA() != null && c.getAnalisisIA().isVisiblePaciente())
                .toList();

        model.addAttribute("consultas", consultas);
        model.addAttribute("userId", patient.getId());
        model.addAttribute("username", patient.getName());

        return "paciente/patientResults";
    }

    @GetMapping("/consultas/{consultaId}/image")
    @ResponseBody
    public ResponseEntity<byte[]> patientConsultaImage(@PathVariable Long consultaId, Authentication auth) {

        if (auth == null) {
            return ResponseEntity.status(403).build();
        }

        User patient = userRepository.findByEmail(auth.getName()).orElse(null);
        if (patient == null || patient.getRole() != Role.PACIENTE) {
            return ResponseEntity.status(403).build();
        }

        Consulta consulta = consultaRepository.findByIdAndPatient(consultaId, patient).orElse(null);
        if (consulta == null || consulta.getAnalisisIA() == null || !consulta.getAnalisisIA().isVisiblePaciente()) {
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
    public ResponseEntity<byte[]> patientConsultaHeatmap(@PathVariable Long consultaId, Authentication auth) {

        if (auth == null) {
            return ResponseEntity.status(403).build();
        }

        User patient = userRepository.findByEmail(auth.getName()).orElse(null);
        if (patient == null || patient.getRole() != Role.PACIENTE) {
            return ResponseEntity.status(403).build();
        }

        Consulta consulta = consultaRepository.findByIdAndPatient(consultaId, patient).orElse(null);
        if (consulta == null || consulta.getAnalisisIA() == null || !consulta.getAnalisisIA().isVisiblePaciente()) {
            return ResponseEntity.status(403).build();
        }

        byte[] heatmap = consulta.getAnalisisIA().getHeatmapImagen();
        if (heatmap == null || heatmap.length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType;
        try {
            mediaType = (consulta.getAnalisisIA().getHeatmapContentType() != null)
                    ? MediaType.parseMediaType(consulta.getAnalisisIA().getHeatmapContentType())
                    : MediaType.IMAGE_PNG;
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok().contentType(mediaType).body(heatmap);
    }
}
