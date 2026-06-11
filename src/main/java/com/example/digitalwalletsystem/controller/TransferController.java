package com.example.digitalwalletsystem.controller;

import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.service.AccountService;
import com.example.digitalwalletsystem.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class TransferController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    public TransferController(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @GetMapping("/transfer")
    public String transferPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<Account> accounts = accountService.getAccountsByUser(userId);
        model.addAttribute("accounts", accounts);
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "transfer";
    }

    @PostMapping("/transfer")
    public String doTransfer(@RequestParam Long fromAccountId,
                             @RequestParam Long toAccountId,
                             @RequestParam BigDecimal amount,
                             @RequestParam(defaultValue = "") String description,
                             @RequestParam(defaultValue = "other") String category,
                             HttpSession session,
                             Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            if (fromAccountId.equals(toAccountId)) {
                throw new RuntimeException("Cannot transfer to the same account");
            }
            transactionService.transfer(fromAccountId, toAccountId, amount, description, category);
            return "redirect:/transfer?success";
        } catch (Exception e) {
            List<Account> accounts = accountService.getAccountsByUser(userId);
            model.addAttribute("accounts", accounts);
            model.addAttribute("userEmail", session.getAttribute("userEmail"));
            model.addAttribute("error", e.getMessage());
            return "transfer";
        }
    }
}