package es.marta.tfg.carescan.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.marta.tfg.carescan.model.Consulta;
import es.marta.tfg.carescan.model.User;
import es.marta.tfg.carescan.repository.ConsultaRepository;
import es.marta.tfg.carescan.repository.UserRepository;

@Controller
@RequestMapping("/upload")
public class AlgorController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

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
            return "redirect:/upload/" + id + "/history?error=empty";
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

        Path uploadPath = Paths.get("uploads", String.valueOf(id));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(safeName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        int randomNumber = new Random().nextInt(100);

        Consulta imagen = new Consulta();
        imagen.setNombreArchivo(safeName);
        imagen.setRuta("/uploads/" + id + "/" + safeName);
        imagen.setResultado(randomNumber);
        imagen.setUser(usuarioAutenticado);
        consultaRepository.save(imagen);

        model.addAttribute("username", usuarioAutenticado.getName());
        model.addAttribute("userId", id);
        redirectAttributes.addFlashAttribute("fileName", safeName);
        redirectAttributes.addFlashAttribute("randomNumber", randomNumber);
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

        List<Consulta> consultas = consultaRepository.findByUserOrderByIdDesc(user);
        model.addAttribute("username", user.getName());
        model.addAttribute("userId", id);
        model.addAttribute("consultas", consultas);
        return "history";
    }

    @GetMapping("/{id}/my-images")
    public String legacyMyImagesRedirect(@PathVariable Long id) {
        return "redirect:/upload/" + id + "/history";
    }

    @GetMapping("/my-images")
    public String redirectToUserImages(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        Optional<User> optionalUser = userRepository.findByEmail(authentication.getName());
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }
        Long id = optionalUser.get().getId();
        return "redirect:/upload/" + id + "/history";
    }

    @GetMapping
    public String uploadRoot(Authentication auth) {
        if (auth != null) {
            Optional<User> opt = userRepository.findByEmail(auth.getName());
            if (opt.isPresent()) {
                return "redirect:/upload/" + opt.get().getId();
            }
        }
        return "redirect:/";
    }

    //Borrar consultas
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

}
