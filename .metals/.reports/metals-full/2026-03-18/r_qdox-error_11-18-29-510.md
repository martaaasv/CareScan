error id: file:///C:/Users/marta/Documents/DOBLE_GRADO/4_carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/service/MedicoApiService.java
file:///C:/Users/marta/Documents/DOBLE_GRADO/4_carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/service/MedicoApiService.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[25,1]

error in qdox parser
file content:
```java
offset: 784
uri: file:///C:/Users/marta/Documents/DOBLE_GRADO/4_carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/service/MedicoApiService.java
text:
```scala
package es.marta.tfg.carescan.service;

import java.util.List;

import org.springframework.stereotype.Service;

import es.marta.tfg.carescan.DTO.ConsultaMedicaResponseDTO;
import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.ConsultaRepository;
import es.marta.tfg.carescan.repository.UserRepository;

@Service
public class MedicoApiService {

    private final ConsultaRepository consultaRepository;
    private final UserRepository userRepository;

    public MedicoApiService(ConsultaRepository consultaRepository,
                            UserRepository userRepository) {
        this.consultaRepository = consultaRepository;
        this.userRepository = userRepository;
    }

 @@   public List<ConsultaMedicaResponseDTO> obtenerConsultasDelMedico(String email) {
        User medico = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        List<Consulta> consultas = consultaRepository.findByUserOrderByFechaHoraDesc(medico);

        return consultas.stream()
                .map(c -> new ConsultaMedicaResponseDTO(
                        c.getId(),
                        c.getFechaHora(),
                        c.getPatient().getName(),
                        "
                ))
                .toList();
    }
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
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:940)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	java.base/java.lang.Thread.run(Thread.java:1575)
```
#### Short summary: 

QDox parse error in file:///C:/Users/marta/Documents/DOBLE_GRADO/4_carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/service/MedicoApiService.java