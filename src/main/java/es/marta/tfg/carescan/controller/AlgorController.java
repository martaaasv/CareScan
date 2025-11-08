package es.marta.tfg.carescan.controller;


import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/upload")
public class AlgorController {

    @RequestMapping
    public String show() {
        return "upload";
    }

    @PostMapping("/image")
    public String uploadImage(MultipartFile file, Model model) {
        if (file != null && !file.isEmpty()) {

            int randomNumber = new Random().nextInt(1000); // entre 0 y 999
            model.addAttribute("randomNumber", randomNumber);
            model.addAttribute("fileName", file.getOriginalFilename());
        } else {
            model.addAttribute("error", "No se ha seleccionado ninguna imagen");
        }
        return "results";
    }
}
