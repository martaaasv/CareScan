package es.marta.tfg.carescan.DTO;

import java.time.LocalDateTime;

public class ConsultaMedicaResponseDTO {

    private Long consultaId;
    private LocalDateTime fecha;
    private String paciente;
    private String motivo;

    public ConsultaMedicaResponseDTO() {
    }

    public ConsultaMedicaResponseDTO(Long consultaId, LocalDateTime fecha, String paciente, String motivo) {
        this.consultaId = consultaId;
        this.fecha = fecha;
        this.paciente = paciente;
        this.motivo = motivo;
    }

    public Long getConsultaId() {
        return consultaId;
    }

    public void setConsultaId(Long consultaId) {
        this.consultaId = consultaId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}