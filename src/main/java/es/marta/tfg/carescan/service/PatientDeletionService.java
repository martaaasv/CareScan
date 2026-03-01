package es.marta.tfg.carescan.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.repository.AnalisisIARepository;
import es.marta.tfg.carescan.repository.ConsultaRepository;
import es.marta.tfg.carescan.repository.DoctorPatientAssignmentRepository;
import es.marta.tfg.carescan.repository.UserRepository;

@Service
public class PatientDeletionService {

    private final UserRepository userRepository;
    private final ConsultaRepository consultaRepository;
    private final AnalisisIARepository analisisIARepository;
    private final DoctorPatientAssignmentRepository assignmentRepository;

    public PatientDeletionService(UserRepository userRepository,
                                  ConsultaRepository consultaRepository,
                                  AnalisisIARepository analisisIARepository,
                                  DoctorPatientAssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.consultaRepository = consultaRepository;
        this.analisisIARepository = analisisIARepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public void hardDeletePatient(Long patientId) {

        // 1) Consultas del paciente (incluye radiografías porque están como bytes en Consulta)
        List<Consulta> consultas = consultaRepository.findAllByPatientId(patientId);

        // 2) AnalisisIA (si está en tabla separada y referencia a Consulta)
        //    Borramos primero para evitar FK
        if (!consultas.isEmpty()) {
            analisisIARepository.deleteByConsultaIn(consultas);
        }

        // 3) Borrar consultas
        consultaRepository.deleteAll(consultas);

        // 4) Borrar asignaciones doctor-paciente (activas o históricas)
        assignmentRepository.deleteByPatientId(patientId);

        // 5) Finalmente borrar el paciente
        userRepository.deleteById(patientId);
    }
}