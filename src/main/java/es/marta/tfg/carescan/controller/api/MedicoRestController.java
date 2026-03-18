package es.marta.tfg.carescan.controller.api;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marta.tfg.carescan.DTO.ConsultaMedicaResponseDTO;
import es.marta.tfg.carescan.service.MedicoApiService;

@RestController
@RequestMapping("/api/medico")
public class MedicoRestController {

    private final MedicoApiService medicoApiService;

    public MedicoRestController(MedicoApiService medicoApiService) {
        this.medicoApiService = medicoApiService;
    }

    @GetMapping("/ping")
    public String ping(Authentication authentication) {
        return "Acceso permitido para: " + authentication.getName();
    }

    @GetMapping("/secure-data")
    public List<ConsultaMedicaResponseDTO> secureData(Authentication authentication) {
        String email = authentication.getName();
        return medicoApiService.obtenerConsultasDelMedico(email);
    }
}
