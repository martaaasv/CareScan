package es.marta.tfg.carescan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.User;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByUserOrderByFechaHoraDesc(User user);

    void deleteByUser(User user);

    List<Consulta> findByUserAndPatientOrderByFechaHoraDesc(User doc, User pat);

    Optional<Consulta> findByIdAndUserAndPatient(Long id, User doctor, User patient);

    long countByUser(User user);

    List<Consulta> findAllByPatientId(Long patientId);

    List<Consulta> findAllByUserId(Long userId);
}
