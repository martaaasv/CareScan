package es.marta.tfg.carescan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.User;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    //List<Consulta> findByUserOrderByIdDesc(User user);
    List<Consulta> findByUserOrderByFechaHoraDesc(User user);

}
