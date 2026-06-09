package com.example.digitalwalletsystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");


        if (email == null) {
            return "redirect:/login";
        }

        model.addAttribute("userEmail", email);
        return "dashboard";
    }
}