package es.marta.tfg.carescan.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

        List<Consulta> consultas = consultaRepository.findAll()
                .stream()
                .filter(c -> c.getPatient().getId().equals(patient.getId()))
                .filter(c -> c.getAnalisisIA() != null && c.getAnalisisIA().isVisiblePaciente())
                .toList();

        model.addAttribute("consultas", consultas);
        model.addAttribute("username", patient.getName());

        return "paciente/patientResults";
    }
}
