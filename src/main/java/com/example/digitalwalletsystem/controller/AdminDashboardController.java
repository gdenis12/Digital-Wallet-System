package com.example.digitalwalletsystem.controller;

import com.example.digitalwalletsystem.model.User;
import com.example.digitalwalletsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminDashboardController {
    private final UserService userService;


    public AdminDashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        String role = (String) session.getAttribute("userRole");

        if (email == null || !"ADMIN".equals(role)) {
            return "redirect:/login";
        }


        long totalUsers = userService.countTotalUsers();
        List<User> usersList = userService.getAllUsers();


        model.addAttribute("adminEmail", email);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("users", usersList);


        model.addAttribute("systemVolume", 150000);

        return "admin-dashboard";
    }


    @PostMapping("/admin/users/block")
    public String blockUser(@RequestParam("userId") Long userId) {
        userService.blockUser(userId);
        return "redirect:/admin/dashboard";
    }


    @PostMapping("/admin/users/unblock")
    public String unblockUser(@RequestParam("userId") Long userId) {
        userService.unblockUser(userId);
        return "redirect:/admin/dashboard";
    }
}
