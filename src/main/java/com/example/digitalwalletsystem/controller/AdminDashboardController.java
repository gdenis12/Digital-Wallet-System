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

    // Внедряем UserService через конструктор
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

        // 1. Вытягиваем реальную статистику
        long totalUsers = userService.countTotalUsers();
        List<User> usersList = userService.getAllUsers();

        // 2. Передаем всё в Thymeleaf
        model.addAttribute("adminEmail", email);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("users", usersList);

        // Временная заглушка для объема системы (можно заменить на реальный подсчет из AccountRepository)
        model.addAttribute("systemVolume", 150000);

        return "admin-dashboard";
    }

    // Обработчик блокировки
    @PostMapping("/admin/users/block")
    public String blockUser(@RequestParam("userId") Long userId) {
        userService.blockUser(userId);
        return "redirect:/admin/dashboard"; // Перезагружаем страницу, чтобы увидеть изменения
    }

    // Обработчик разблокировки
    @PostMapping("/admin/users/unblock")
    public String unblockUser(@RequestParam("userId") Long userId) {
        userService.unblockUser(userId);
        return "redirect:/admin/dashboard";
    }
}
