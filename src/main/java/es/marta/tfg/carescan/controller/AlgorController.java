package es.marta.tfg.carescan.controller;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.AnalisisIARepository;
import es.marta.tfg.carescan.repository.ConsultaRepository;
import es.marta.tfg.carescan.repository.UserRepository;
import es.marta.tfg.carescan.service.BrainTumorAnalysisService;
import es.marta.tfg.carescan.service.BrainTumorPrediction;

@Controller
@RequestMapping("/upload")
public class AlgorController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private AnalisisIARepository analisisIARepository;

    @Autowired
    private BrainTumorAnalysisService brainTumorAnalysisService;

    @GetMapping("/{id}")
    public String showUserUpload(@PathVariable Long id, Model model) {
        model.addAttribute("username", userRepository.findById(id).map(User::getName).orElse("Usuario"));
        model.addAttribute("userId", id);
        return "upload";
    }

    @GetMapping("/{id}/upload")
    public String redirectUploadGet(@PathVariable Long id) {
        return "redirect:/upload/" + id;
    }

    @PostMapping("/{id}/upload")
    public String uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes, Model model) throws IOException {

        if (authentication == null) {
            return "redirect:/login";
        }
        if (file == null || file.isEmpty()) {
            return "redirect:/upload/" + id;
        }

        String email = authentication.getName();
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User usuarioAutenticado = optionalUser.get();
        if (!usuarioAutenticado.getId().equals(id)) {
            return "error/403";
        }

        String originalName = file.getOriginalFilename();
        String safeName = (originalName == null) ? "archivo" : Paths.get(originalName).getFileName().toString();

        Consulta imagen = new Consulta();
        imagen.setNombreArchivo(safeName);
        imagen.setUser(usuarioAutenticado);
        imagen.setImagen(file.getBytes());
        imagen.setContentType(file.getContentType());
        imagen.setFechaHora(LocalDateTime.now());

        Consulta saved = consultaRepository.save(imagen);
        BrainTumorPrediction prediction = brainTumorAnalysisService.predict(saved.getImagen());
        saved.setResultado((int) Math.round(prediction.confidence() * 100));
        consultaRepository.save(saved);

        analisisIARepository.save(createAnalysis(saved, prediction));

        model.addAttribute("username", usuarioAutenticado.getName());
        model.addAttribute("userId", id);
        redirectAttributes.addFlashAttribute("fileName", safeName);
        redirectAttributes.addFlashAttribute("predictionLabel", prediction.label());
        redirectAttributes.addFlashAttribute("predictionConfidence", prediction.confidence());
        redirectAttributes.addFlashAttribute("userId", id);

        return "redirect:/upload/" + id + "/results";
    }

    @GetMapping("/{id}/results")
    public String showResults(@PathVariable Long id, Model model) {
        model.addAttribute("username", userRepository.findById(id).map(User::getName).orElse("Usuario"));
        if (!model.containsAttribute("userId")) {
            return "redirect:/upload/" + id;
        }
        return "results";
    }

    @GetMapping("/{id}/history")
    public String history(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Optional<User> optionalUser = userRepository.findByEmail(authentication.getName());
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        if (!user.getId().equals(id)) {
            return "error/403";
        }

        List<Consulta> consultas = consultaRepository.findByUserOrderByFechaHoraDesc(user);
        model.addAttribute("username", user.getName());
        model.addAttribute("userId", id);
        model.addAttribute("consultas", consultas);
        return "history";
    }
    
    @PostMapping("/{id}/history/delete")
    public String deleteConsulta(
            @PathVariable Long id,
            @RequestParam("consultaId") Long consultaId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null) {
            return "redirect:/login";
        }

        Optional<User> optionalUser = userRepository.findByEmail(authentication.getName());
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();

        if (!user.getId().equals(id)) {
            return "error/403";
        }

        Optional<Consulta> optionalConsulta = consultaRepository.findById(consultaId);
        if (optionalConsulta.isPresent()) {
            Consulta consulta = optionalConsulta.get();
            if (consulta.getUser().getId().equals(user.getId())) {
                consultaRepository.delete(consulta);
                redirectAttributes.addFlashAttribute("mensajeExito", "La consulta se ha borrado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("mensajeError", "No puedes borrar consultas de otro usuario.");
            }
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "La consulta indicada no existe.");
        }

        return "redirect:/upload/" + id + "/history";
    }

   
    @GetMapping("/consulta/{consultaId}/imagen")
    public ResponseEntity<byte[]> verImagen(
            @PathVariable Long consultaId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        Optional<User> optionalUser = userRepository.findByEmail(authentication.getName());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        User user = optionalUser.get();

        Optional<Consulta> optionalConsulta = consultaRepository.findById(consultaId);
        if (optionalConsulta.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Consulta consulta = optionalConsulta.get();

        if (!consulta.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        if (consulta.getImagen() == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = consulta.getContentType();
        MediaType mediaType = (contentType != null)
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(consulta.getImagen());
    }

    private es.marta.tfg.carescan.model.AnalisisIA createAnalysis(Consulta consulta, BrainTumorPrediction prediction) {
        es.marta.tfg.carescan.model.AnalisisIA analisis = new es.marta.tfg.carescan.model.AnalisisIA();
        analisis.setConsulta(consulta);
        analisis.setEtiqueta(prediction.label());
        analisis.setProbabilidad(prediction.confidence());
        analisis.setModeloVersion(prediction.modelVersion());
        analisis.setEjecutadoEn(LocalDateTime.now());
        analisis.setVisiblePaciente(false);
        return analisis;
    }
}
