package com.example.digitalwalletsystem.controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    private Long getUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    @GetMapping("/accounts")
    public String accounts(HttpSession session, Model model) {
        Long userId = getUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            List<Account> accounts = accountService.getAccountsByUser(userId);
            model.addAttribute("accounts", accounts);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load accounts: " + e.getMessage());
            model.addAttribute("accounts", List.of());
        }
        return "accounts";
    }

    @GetMapping("/accounts/create")
    public String createPage(HttpSession session) {
        if (getUserId(session) == null) {
            return "redirect:/login";
        }
        return "account-create";
    }

    @PostMapping("/accounts/create")
    public String createAccount(@RequestParam String type,
                                @RequestParam String currency,
                                HttpSession session,
                                Model model) {
        Long userId = getUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            Account account = new Account();
            account.setType(type);
            account.setCurrency(currency);
            accountService.createAccount(account, userId);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/accounts";
    }

    @PostMapping("/accounts/deposit")
    public String deposit(@RequestParam Long accountId,
                          @RequestParam BigDecimal amount,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (getUserId(session) == null) return "redirect:/login";
        try {
            accountService.deposit(accountId, amount);
            redirectAttributes.addFlashAttribute("success", "Deposit successful");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/accounts";
    }

    @PostMapping("/accounts/withdraw")
    public String withdraw(@RequestParam Long accountId,
                           @RequestParam BigDecimal amount,
                           HttpSession session) {
        if (getUserId(session) == null) {
            return "redirect:/login";
        }
        accountService.withdraw(accountId, amount);
        return "redirect:/accounts";
    }

    @PostMapping("/accounts/block/{id}")
    public String block(@PathVariable Long id, HttpSession session) {
        if (getUserId(session) == null) return "redirect:/login";
        accountService.blockAccount(id);
        return "redirect:/accounts";
    }

    @PostMapping("/accounts/toggle-block/{id}")
    public String toggleBlock(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (getUserId(session) == null) return "redirect:/login";
        try {
            accountService.toggleBlock(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/accounts";
    }

    @GetMapping("/admin/accounts")
    public String adminAccounts(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        if (userId == null) return "redirect:/login";
        if (!"ADMIN".equals(userRole)) return "redirect:/dashboard";

        try {

            List<Account> allAccounts = accountService.getAccountsByUser(null);
            model.addAttribute("accounts", allAccounts);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load global accounts: " + e.getMessage());
            model.addAttribute("accounts", List.of());
        }

        return "admin-accounts";
    }
}