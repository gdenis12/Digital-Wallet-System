package com.example.digitalwalletsystem.controller;

import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.model.Transaction;
import com.example.digitalwalletsystem.service.AccountService;
import com.example.digitalwalletsystem.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
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
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) LocalDate from,
                               @RequestParam(required = false) LocalDate to,
                               HttpSession session,
                               Model model,
                               HttpServletRequest request) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));

        List<Account> accounts = accountService.getAccountsByUser(userId);
        model.addAttribute("accounts", accounts);


        List<Transaction> transactions = transactionService.getFilteredHistory(userId, accountId, type, category, from, to);
        model.addAttribute("transactions", transactions);


        BigDecimal totalIn = BigDecimal.ZERO;
        BigDecimal totalOut = BigDecimal.ZERO;

        for (Transaction tx : transactions) {
            if ("SUCCESS".equalsIgnoreCase(tx.getStatus()) || "COMPLETED".equalsIgnoreCase(tx.getStatus())) {
                if (accountId != null) {
                    if (tx.getToAccount() != null && tx.getToAccount().getId().equals(accountId)) {
                        totalIn = totalIn.add(tx.getAmount());
                    } else if (tx.getFromAccount() != null && tx.getFromAccount().getId().equals(accountId)) {
                        totalOut = totalOut.add(tx.getAmount());
                    }
                } else {
                    String txType = tx.getType();
                    if ("INCOME".equals(txType) || "DEPOSIT".equals(txType)) {
                        totalIn = totalIn.add(tx.getAmount());
                    } else if ("EXPENSE".equals(txType) || "WITHDRAWAL".equals(txType)) {
                        totalOut = totalOut.add(tx.getAmount());
                    } else if ("TRANSFER".equals(txType)) {
                        totalIn = totalIn.add(tx.getAmount());
                        totalOut = totalOut.add(tx.getAmount());
                    }
                }
            }
        }

        model.addAttribute("totalCount", transactions.size());
        model.addAttribute("totalIn", totalIn);
        model.addAttribute("totalOut", totalOut);

        return "transactions";
    }

    @GetMapping("/admin/transactions")
    public String adminTransactions(@RequestParam(required = false) String type,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) LocalDate from,
                                    @RequestParam(required = false) LocalDate to,
                                    HttpSession session,
                                    Model model,
                                    HttpServletRequest request) {

        Long userId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        if (userId == null) return "redirect:/login";
        if (!"ADMIN".equals(userRole)) return "redirect:/dashboard";

        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        model.addAttribute("accounts", List.of());


        List<Transaction> transactions = transactionService.getFilteredHistory(null, null, type, category, from, to);
        model.addAttribute("transactions", transactions);

        BigDecimal totalIn = BigDecimal.ZERO;
        BigDecimal totalOut = BigDecimal.ZERO;

        for (Transaction tx : transactions) {
            if ("SUCCESS".equalsIgnoreCase(tx.getStatus()) || "COMPLETED".equalsIgnoreCase(tx.getStatus())) {
                String txType = tx.getType();
                if ("INCOME".equals(txType) || "DEPOSIT".equals(txType)) {
                    totalIn = totalIn.add(tx.getAmount());
                } else if ("EXPENSE".equals(txType) || "WITHDRAWAL".equals(txType)) {
                    totalOut = totalOut.add(tx.getAmount());
                } else if ("TRANSFER".equals(txType)) {
                    totalIn = totalIn.add(tx.getAmount());
                    totalOut = totalOut.add(tx.getAmount());
                }
            }
        }

        model.addAttribute("totalCount", transactions.size());
        model.addAttribute("totalIn", totalIn);
        model.addAttribute("totalOut", totalOut);

        return "admin-transactions";
    }
}