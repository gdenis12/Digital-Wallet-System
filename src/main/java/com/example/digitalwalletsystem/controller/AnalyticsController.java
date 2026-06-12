package com.example.digitalwalletsystem.controller;

import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.model.Transaction;
import com.example.digitalwalletsystem.service.AccountService;
import com.example.digitalwalletsystem.service.AnalyticsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AccountService accountService;

    public AnalyticsController(AnalyticsService analyticsService, AccountService accountService) {
        this.analyticsService = analyticsService;
        this.accountService = accountService;
    }

    private Long getUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    @GetMapping("/analytics")
    public String analytics(HttpSession session,
                            @RequestParam(required = false) Long accountId,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                            Model model) {
        Long userId = getUserId(session);
        if (userId == null) return "redirect:/login";

        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null)   to   = LocalDate.now();

        try {
            List<Account> accounts = accountService.getAccountsByUser(userId);
            model.addAttribute("accounts", accounts);


            List<Transaction> transactions = analyticsService.getTransactions(userId, accountId, from, to);


            model.addAttribute("totalSpent",         analyticsService.getTotalSpent(transactions, accountId));
            model.addAttribute("totalIncome",        analyticsService.getTotalIncome(transactions, accountId));
            model.addAttribute("avgTransaction",     analyticsService.getAvg(transactions));
            model.addAttribute("largestTransaction", analyticsService.getLargest(transactions));
            model.addAttribute("txCount",            transactions.size());

            model.addAttribute("top5",               analyticsService.getTop5(transactions, accountId));
            model.addAttribute("categoryStats",      analyticsService.getCategoryStats(transactions, accountId));
            model.addAttribute("monthLabels",        analyticsService.getMonthLabels(from, to));
            model.addAttribute("monthAmounts",       analyticsService.getMonthAmounts(transactions, from, to, accountId));


            model.addAttribute("categoryLabels",     analyticsService.getCategoryLabels(transactions, accountId));
            model.addAttribute("categoryAmounts",    analyticsService.getCategoryAmounts(transactions, accountId));

        } catch (Exception e) {
            model.addAttribute("error", "Failed to load analytics: " + e.getMessage());
        }

        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        model.addAttribute("selectedAccountId", accountId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "analytics";
    }
}