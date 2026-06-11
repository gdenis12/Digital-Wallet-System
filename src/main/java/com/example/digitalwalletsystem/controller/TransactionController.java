package com.example.digitalwalletsystem.controller;

import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.model.Transaction;
import com.example.digitalwalletsystem.service.AccountService;
import com.example.digitalwalletsystem.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    public TransactionController(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @GetMapping("/transactions")
    public String transactions(@RequestParam(required = false) Long accountId,
                               HttpSession session,
                               Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<Account> accounts = accountService.getAccountsByUser(userId);
        model.addAttribute("accounts", accounts);
        model.addAttribute("userEmail", session.getAttribute("userEmail"));

        if (accountId != null) {
            List<Transaction> transactions = transactionService.getAccountHistory(accountId);
            model.addAttribute("transactions", transactions);
            model.addAttribute("selectedAccountId", accountId);
        } else if (!accounts.isEmpty()) {
            List<Transaction> transactions = transactionService.getAccountHistory(accounts.get(0).getId());
            model.addAttribute("transactions", transactions);
            model.addAttribute("selectedAccountId", accounts.get(0).getId());
        } else {
            model.addAttribute("transactions", List.of());
        }

        return "transactions";
    }
}