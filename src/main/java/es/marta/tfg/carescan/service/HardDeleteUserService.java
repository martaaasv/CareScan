package es.marta.tfg.carescan.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.AnalisisIARepository;
import es.marta.tfg.carescan.repository.ConsultaRepository;
import es.marta.tfg.carescan.repository.DoctorPatientAssignmentRepository;
import es.marta.tfg.carescan.repository.UserRepository;

@Service
public class HardDeleteUserService {

    private final UserRepository userRepository;
    private final ConsultaRepository consultaRepository;
    private final AnalisisIARepository analisisIARepository;
    private final DoctorPatientAssignmentRepository assignmentRepository;

    public HardDeleteUserService(UserRepository userRepository,
            ConsultaRepository consultaRepository,
            AnalisisIARepository analisisIARepository,
            DoctorPatientAssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.consultaRepository = consultaRepository;
        this.analisisIARepository = analisisIARepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public void hardDeleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Role role = user.getRole();

        // 1) Si es PACIENTE: borrar consultas donde es patient + análisis + asignaciones patient
        if (role == Role.PACIENTE) {

            List<Consulta> consultasPaciente = consultaRepository.findAllByPatientId(userId);

            if (!consultasPaciente.isEmpty()) {
                analisisIARepository.deleteByConsultaIn(consultasPaciente);
                consultaRepository.deleteAll(consultasPaciente);
            }

            assignmentRepository.deleteByPatientId(userId);
        }

        // 2) Si es MEDICO: borrar consultas donde es doctor(user) + análisis + asignaciones doctor
        if (role == Role.MEDICO) {

            List<Consulta> consultasMedico = consultaRepository.findAllByUserId(userId);

            if (!consultasMedico.isEmpty()) {
                analisisIARepository.deleteByConsultaIn(consultasMedico);
                consultaRepository.deleteAll(consultasMedico);
            }

            assignmentRepository.deleteByDoctorId(userId);
        }

        // 3) Si es ADMIN_HOSPITAL / ADMIN_IT:
        // normalmente no cuelga nada de ellos. Si tienes tablas relacionadas (logs, etc) aquí se añaden.
        // 4) Finalmente borrar el usuario
        userRepository.deleteById(userId);
    }
}
