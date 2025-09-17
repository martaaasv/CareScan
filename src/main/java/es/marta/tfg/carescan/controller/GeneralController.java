package es.marta.tfg.carescan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GeneralController {

    @GetMapping("/")
    public String homeTest() {
        return "home";
    }
} 
