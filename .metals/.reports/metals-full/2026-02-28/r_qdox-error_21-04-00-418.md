error id: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/service/PatientDeletionService.java
file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/service/PatientDeletionService.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[3,8]

error in qdox parser
file content:
```java
offset: 50
uri: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/service/PatientDeletionService.java
text:
```scala
package es.marta.tfg.carescan.service;

public p@@ackage es.marta.tfg.carescan.service;

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
} {
    
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

QDox parse error in file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/service/PatientDeletionService.java