package com.example.digitalwalletsystem.controller;

import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.model.Transaction;
import com.example.digitalwalletsystem.model.User;
import com.example.digitalwalletsystem.repository.UserRepository;
import com.example.digitalwalletsystem.service.AccountService;
import com.example.digitalwalletsystem.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserRepository userRepository; // Добавляем репозиторий пользователей, чтобы узнать userId

    @Autowired
    public DashboardController(AccountService accountService,
                               TransactionService transactionService,
                               UserRepository userRepository) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");

        // Проверка авторизации
        if (email == null) {
            return "redirect:/login";
        }


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        Long userId = user.getId();


        List<Account> accounts = accountService.getAccountsByUser(userId);


        List<Transaction> transactions = transactionService.getFilteredHistory(
                userId, null, null, null, null, null
        );


        model.addAttribute("userEmail", email);
        model.addAttribute("accounts", accounts);
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentUri", "/dashboard"); // Для подсветки меню

        return "dashboard";
    }
}