package es.marta.tfg.carescan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.marta.tfg.carescan.model.DoctorPatientAssignment;
import es.marta.tfg.carescan.model.User;

public interface DoctorPatientAssignmentRepository extends JpaRepository<DoctorPatientAssignment, Long> {

    Optional<DoctorPatientAssignment> findByPatientAndActiveTrue(User patient);

    List<DoctorPatientAssignment> findByDoctorAndActiveTrue(User doctor);

    Optional<DoctorPatientAssignment> findByDoctorAndPatientAndActiveTrue(User doctor, User patient);

    List<DoctorPatientAssignment> findByPatientInAndActiveTrue(List<User> patients);

    List<DoctorPatientAssignment> findByActiveTrue();

    boolean existsByDoctorAndPatientAndActiveTrue(User doctor, User patient);
}
