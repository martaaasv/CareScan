error id: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/model/Consulta.java:_empty_/GeneratedValue#strategy#
file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/model/Consulta.java
empty definition using pc, found symbol in pc: _empty_/GeneratedValue#strategy#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 528
uri: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/model/Consulta.java
text:
```scala
package es.marta.tfg.carescan.model;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Consulta {

    @Id
    @GeneratedValue(str@@ategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreArchivo;

    private Integer resultado;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imagen;

    private String contentType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // "Dueño" de la radiografía
    @ManyToOne
    @JoinColumn(name = "patientId", nullable = false)
    private User patient;

    private LocalDateTime fechaHora;

    @OneToOne(mappedBy = "consulta", cascade = CascadeType.ALL)
    private AnalisisIA analisisIA;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public Integer getResultado() {
        return resultado;
    }

    public void setResultado(Integer resultado) {
        this.resultado = resultado;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User usuarioAutenticado) {
        this.user = usuarioAutenticado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public AnalisisIA getAnalisisIA() {
        return analisisIA;
    }

    public void setAnalisisIA(AnalisisIA analisisIA) {
        this.analisisIA = analisisIA;
    }

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/GeneratedValue#strategy#