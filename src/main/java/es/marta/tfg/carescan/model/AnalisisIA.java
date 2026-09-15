package es.marta.tfg.carescan.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;

@Entity
public class AnalisisIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "consultaId", nullable = false, unique = true)
    private Consulta consulta;

    @Column(nullable = false)
    private double probabilidad;

    @Column(nullable = false)
    private String etiqueta;

    @Column(nullable = false)
    private String modeloVersion;

    @Column(nullable = false)
    private LocalDateTime ejecutadoEn;

    @Column(nullable = false)
    private boolean visiblePaciente = false;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String comentarioMedico;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] heatmapImagen;

    private String heatmapContentType;

    public AnalisisIA() {
    }

    public Long getId() {
        return id;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public double getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(double probabilidad) {
        this.probabilidad = probabilidad;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getModeloVersion() {
        return modeloVersion;
    }

    public void setModeloVersion(String modeloVersion) {
        this.modeloVersion = modeloVersion;
    }

    public LocalDateTime getEjecutadoEn() {
        return ejecutadoEn;
    }

    public void setEjecutadoEn(LocalDateTime ejecutadoEn) {
        this.ejecutadoEn = ejecutadoEn;
    }

    public boolean isVisiblePaciente() {
        return visiblePaciente;
    }

    public void setVisiblePaciente(boolean visiblePaciente) {
        this.visiblePaciente = visiblePaciente;
    }

    public String getComentarioMedico() {
        return comentarioMedico;
    }

    public void setComentarioMedico(String comentarioMedico) {
        this.comentarioMedico = comentarioMedico;
    }

    public byte[] getHeatmapImagen() {
        return heatmapImagen;
    }

    public void setHeatmapImagen(byte[] heatmapImagen) {
        this.heatmapImagen = heatmapImagen;
    }

    public String getHeatmapContentType() {
        return heatmapContentType;
    }

    public void setHeatmapContentType(String heatmapContentType) {
        this.heatmapContentType = heatmapContentType;
    }

}
