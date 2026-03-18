package es.marta.tfg.carescan.service;

import java.util.List;

import org.springframework.stereotype.Service;

import es.marta.tfg.carescan.DTO.ConsultaMedicaResponseDTO;
import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.ConsultaRepository;
import es.marta.tfg.carescan.repository.UserRepository;

@Service
public class MedicoApiService {

    private final ConsultaRepository consultaRepository;
    private final UserRepository userRepository;

    public MedicoApiService(ConsultaRepository consultaRepository,
                            UserRepository userRepository) {
        this.consultaRepository = consultaRepository;
        this.userRepository = userRepository;
    }

    public List<ConsultaMedicaResponseDTO> obtenerConsultasDelMedico(String email) {
        User medico = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        List<Consulta> consultas = consultaRepository.findByUserOrderByFechaHoraDesc(medico);

        return consultas.stream()
                .map(c -> new ConsultaMedicaResponseDTO(
                        c.getId(),
                        c.getFechaHora(),
                        c.getPatient().getName(),
                        "Consulta médica"
                ))
                .toList();
    }
}

