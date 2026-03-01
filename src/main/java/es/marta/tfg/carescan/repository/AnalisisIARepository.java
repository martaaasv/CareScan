package es.marta.tfg.carescan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.marta.tfg.carescan.model.AnalisisIA;
import es.marta.tfg.carescan.model.Consulta;

public interface AnalisisIARepository extends JpaRepository<AnalisisIA, Long> {
    void deleteByConsultaIn(List<Consulta> consultas);
}
