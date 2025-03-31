package com.thecritics.reorder.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thecritics.reorder.service.OrdererService;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/signup")
public class OrdererSignupController {

    @Autowired
    OrdererService ordererService;

    @GetMapping
    public String signupOrderer() {
        return "signup";
    }

    @PostMapping("/upload")
    public String uploadOrderer(@RequestParam String username, @RequestParam String email, @RequestParam String password, Model model){
        if (username == null || username.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()) {
            model.addAttribute("errorMessage", "Todos los campos son obligatorios");
            return "signup";
        }

        if (ordererService.emailAlreadyTaken(email)) {
            model.addAttribute("errorMessage", "El correo ya está registrado. Intente con otro.");
            return "signup";
        }

        if (ordererService.usernameAlreadyTaken(username)) {
            model.addAttribute("errorMessage", "El nombre de usuario ya está registrado. Intente con otro.");
            return "signup";
        }

        ordererService.saveOrderer(email, username, password);

        return "redirect:/";
    }
}
