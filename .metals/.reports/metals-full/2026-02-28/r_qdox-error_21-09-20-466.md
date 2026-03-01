error id: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/AdminController.java
file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/AdminController.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[146,1]

error in qdox parser
file content:
```java
offset: 5367
uri: file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/AdminController.java
text:
```scala
package es.marta.tfg.carescan.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.marta.tfg.carescan.model.Estado;
import es.marta.tfg.carescan.model.Role;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.ConsultaRepository;
import es.marta.tfg.carescan.repository.UserRepository;
import es.marta.tfg.carescan.service.PatientDeletionService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsultaRepository consultaRepository;
    
    @Autowired
    private PatientDeletionService patientDeletionService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Authentication auth) {

        List<User> users = userRepository.findAll();

        model.addAttribute("users", users);

        long totalUsuarios = users.size();
        long totalConsultas = consultaRepository.count();

        Map<Long, Long> consultasCount = new LinkedHashMap<>();
        for (User u : users) {
            consultasCount.put(u.getId(), consultaRepository.countByUser(u));
        }

        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalConsultas", totalConsultas);
        model.addAttribute("consultasCount", consultasCount);

        User admin = userRepository.findByEmail(auth.getName()).get();
        model.addAttribute("userId", admin.getId());
        model.addAttribute("username", admin.getName());

        return "admin-dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model, Authentication auth) {

        List<User> users = userRepository.findAll();

        Map<Long, Long> consultasCount = new LinkedHashMap<>();
        for (User u : users) {
            long count = consultaRepository.countByUser(u);
            consultasCount.put(u.getId(), count);
        }

        model.addAttribute("users", users);
        model.addAttribute("consultasCount", consultasCount);
        User admin = userRepository.findByEmail(auth.getName()).get();
        model.addAttribute("userId", admin.getId());
        model.addAttribute("username", admin.getName());

        return "admin-users";
    }

    @PostMapping("/users/{id}/role")
    public String changeUserRole(
            @PathVariable Long id,
            @RequestParam("role") Role role,
            RedirectAttributes redirectAttributes) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El usuario no existe.");
            return "redirect:/admin/users";
        }

        User user = optionalUser.get();
        user.setRole(role);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("mensajeExito", "Rol actualizado correctamente.");
        return "redirect:/admin/users";
    }

  /*   @PostMapping("/users/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "El usuario no existe.");
            return "redirect:/admin/users";
        }

        User user = optionalUser.get();

        consultaRepository.deleteByUser(user);
        userRepository.delete(user);

        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario borrado correctamente.");
        return "redirect:/admin/users";
    }

    @PostMapping("/patients/{patientId}/delete")
    public String deletePatient(@PathVariable Long patientId, RedirectAttributes ra) {

        User patient = userRepository.findById(patientId).orElse(null);
        if (patient == null || patient.getRole() != Role.PACIENTE) {
            ra.addFlashAttribute("error", "Paciente no encontrado.");
            return "redirect:/admin-hospital/patients";
        }

        // Solo permitimos borrar duro si ya está INACTIVO
        if (patient.getEstado() != Estado.INACTIVO) {
            ra.addFlashAttribute("error", "Solo se puede eliminar definitivamente un paciente INACTIVO.");
            return "redirect:/admin-hospital/patients";
        }

        patientDeletionService.hardDeletePatient(patientId);

        ra.addFlashAttribute("success", "Paciente eliminado definitivamente (consultas, radiografías, asignaciones, etc.).");
        return "redirect:/admin-hospital/patients";
    }
}
@@
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

QDox parse error in file:///C:/Users/marta/Documents/DOBLE%20GRADO/4º%20carrera/TFG/WEB/TFG/src/main/java/es/marta/tfg/carescan/controller/AdminController.java