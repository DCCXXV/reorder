package com.thecritics.reorder.controller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thecritics.reorder.model.Orderer;
import com.thecritics.reorder.repository.OrdererRepository;
import com.thecritics.reorder.service.OrdererService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/login")
public class OrdererLoginController {

    private static final Logger log = LogManager.getLogger(OrderCreationController.class);

    @Autowired
    private OrdererService ordererService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping
    public String login(Model model){
        return "login";
    }
 
    @PostMapping("/upload")
    public String loginUser(@RequestParam String identifier, @RequestParam String password, Model model,HttpSession session) {
        if (identifier == null || identifier.isEmpty()){
            model.addAttribute("errorMessage", "introduzca su usuario o email");
            return "login";        }
        if (password == null || password.isEmpty()){
            model.addAttribute("errorMessage", "introduzca su contraseña");
            return "login";        }

        boolean isEmail = identifier.contains("@");
        Orderer orderer = null;
        if(isEmail) {
            orderer = ordererService.findByEmail(identifier);
        } else {
            orderer = ordererService.findByUsername(identifier);
        }
        //una vez que tenemos login exito se dirige a la pagina de inicio 
        if(orderer == null) {
            model.addAttribute("errorMessage", "¡El usuario no existe!");
            return "login";
        } else if (!passwordEncoder.matches(password, orderer.getPassword())) {
    
            model.addAttribute("errorMessage", "¡Contraseña incorrecta!");
            return "login";
        } else {
            session.setAttribute("u", orderer.getUsername());
            session.setAttribute("ws", orderer.getId());
            session.setAttribute("orderer", orderer);
            return "redirect:/";
        }
        
    }
}

