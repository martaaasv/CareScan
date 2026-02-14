error id: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/MedicoController.java
file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/MedicoController.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[41,5]

error in qdox parser
file content:
```java
offset: 1365
uri: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/MedicoController.java
text:
```scala
package es.marta.tfg.carescan.controller;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.marta.tfg.carescan.model.AnalisisIA;
import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.AnalisisIARepository;
import es.marta.tfg.carescan.repository.ConsultaRepository;
import es.marta.tfg.carescan.repository.UserRepository;

@Controller
@RequestMapping("/medico")
public class MedicoController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private AnalisisIARepository analisisIARepository;

    @Autowired
    priva

    @@@GetMapping("/patients")
    public String patientList(Model model, Authentication auth) {

        if (auth == null) {
            return "redirect:/login";
        }

        User doctor = userRepository.findByEmail(auth.getName()).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return "redirect:/access-denied";
        }

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("patients", doctor.getPatients());

        return "medico/patientList";
    }

    // Subir la radiografía de un paciente 
    @GetMapping("/patients/{patientId}/upload")
    public String uploadRadiographyForm(@PathVariable Long patientId, Model model, Authentication auth) {

        if (auth == null) {
            return "redirect:/login";
        }

        User doctor = userRepository.findByEmail(auth.getName()).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return "redirect:/access-denied";
        }

        User patient = userRepository.findById(patientId).orElse(null);
        if (patient == null || patient.getRole() != Role.PACIENTE) {
            return "redirect:/access-denied";
        }

        if (!doctor.getPatients().contains(patient)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("patient", patient);

        return "medico/uploadRadiography";
    }

    @PostMapping("/patients/{patientId}/upload")
    public String uploadRadiography(@PathVariable Long patientId,
            Authentication auth,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (auth == null) {
            return "redirect:/login";
        }

        User doctor = userRepository.findByEmail(auth.getName()).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return "redirect:/access-denied";
        }

        User patient = userRepository.findById(patientId).orElse(null);
        if (patient == null || patient.getRole() != Role.PACIENTE) {
            return "redirect:/access-denied";
        }

        if (!doctor.getPatients().contains(patient)) {
            return "redirect:/access-denied";
        }

        if (file == null || file.isEmpty()) {
            return "redirect:/medico/patients/" + patientId + "/upload";
        }

        Consulta consulta = new Consulta();
        consulta.setUser(doctor);
        consulta.setPatient(patient);
        consulta.setFechaHora(LocalDateTime.now());
        consulta.setContentType(file.getContentType());
        consulta.setImagen(file.getBytes());
        Consulta saved = consultaRepository.save(consulta);

        double prob = Math.random();
        String etiqueta = prob < 0.33 ? "NEGATIVO" : (prob < 0.66 ? "SOSPECHOSO" : "POSITIVO");

        AnalisisIA analisis = new AnalisisIA();
        analisis.setConsulta(saved);
        analisis.setProbabilidad(prob);
        analisis.setEtiqueta(etiqueta);
        analisis.setModeloVersion("stub-v1");
        analisis.setEjecutadoEn(LocalDateTime.now());

        analisisIARepository.save(analisis);

        return "redirect:/medico/consultas/" + saved.getId() + "/results";

    }

    @GetMapping("/consultas/{consultaId}/results")
    public String consultaResults(@PathVariable Long consultaId, Model model, Authentication auth) {

        if (auth == null) {
            return "redirect:/login";
        }

        User doctor = userRepository.findByEmail(auth.getName()).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return "redirect:/access-denied";
        }

        Consulta consulta = consultaRepository.findById(consultaId).orElse(null);
        if (consulta == null) {
            return "redirect:/access-denied";
        }

        if (!consulta.getUser().getId().equals(doctor.getId())) {
            return "redirect:/access-denied";
        }

        model.addAttribute("userId", doctor.getId());
        model.addAttribute("username", doctor.getName());
        model.addAttribute("consulta", consulta);
        model.addAttribute("patient", consulta.getPatient());

        return "medico/caseResults";
    }

    @PostMapping("/consultas/{consultaId}/publish")
    public String publishResult(@PathVariable Long consultaId, Authentication auth) {

        if (auth == null) {
            return "redirect:/login";
        }

        User doctor = userRepository.findByEmail(auth.getName()).orElse(null);
        if (doctor == null || doctor.getRole() != Role.MEDICO) {
            return "redirect:/access-denied";
        }

        Consulta consulta = consultaRepository.findById(consultaId).orElse(null);
        if (consulta == null) {
            return "redirect:/access-denied";
        }

        if (!consulta.getUser().getId().equals(doctor.getId())) {
            return "redirect:/access-denied";
        }

        AnalisisIA analisis = consulta.getAnalisisIA();
        if (analisis != null) {
            analisis.setVisiblePaciente(true);
            analisisIARepository.save(analisis);
        }

        return "redirect:/medico/consultas/" + consultaId + "/results";
    }

}

```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:560)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:691)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:688)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:688)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:936)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	java.base/java.lang.Thread.run(Thread.java:1575)
```
#### Short summary: 

QDox parse error in file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/MedicoController.java