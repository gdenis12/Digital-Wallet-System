package com.example.digitalwalletsystem.controller;

import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.model.Transaction;
import com.example.digitalwalletsystem.service.AccountService;
import com.example.digitalwalletsystem.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // НОВЫЙ ЭНДПОИНТ ДЛЯ ВЫГРУЗКИ КОМПЛЕКСНОГО ОТЧЕТА В ТЕКСТОВЫЙ ФАЙЛ
    @GetMapping("/transactions/export")
    public void exportFullFinancialReport(HttpSession session, HttpServletResponse response) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        String email = (String) session.getAttribute("userEmail");

        if (userId == null || email == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        // 1. Сбор базовых финансовых данных пользователя
        List<Account> accounts = accountService.getAccountsByUser(userId);
        List<Transaction> allTransactions = transactionService.getFilteredHistory(userId, null, null, null, null, null);

        // 2. Расчет месячного финансового отчета
        BigDecimal monthlyIncome = BigDecimal.ZERO;
        BigDecimal monthlyExpense = BigDecimal.ZERO;
        LocalDate now = LocalDate.now();

        for (Transaction tx : allTransactions) {
            if ("SUCCESS".equalsIgnoreCase(tx.getStatus()) || "COMPLETED".equalsIgnoreCase(tx.getStatus())) {
                if (tx.getTimestamp() != null && tx.getTimestamp().getMonth() == now.getMonth() && tx.getTimestamp().getYear() == now.getYear()) {
                    String txType = tx.getType();
                    if ("INCOME".equals(txType) || "DEPOSIT".equals(txType)) {
                        monthlyIncome = monthlyIncome.add(tx.getAmount());
                    } else if ("EXPENSE".equals(txType) || "WITHDRAWAL".equals(txType)) {
                        monthlyExpense = monthlyExpense.add(tx.getAmount());
                    } else if ("TRANSFER".equals(txType)) {
                        monthlyIncome = monthlyIncome.add(tx.getAmount());
                        monthlyExpense = monthlyExpense.add(tx.getAmount());
                    }
                }
            }
        }

        // 3. Конфигурация HTTP-ответа
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"financial_report_" + now + ".txt\"");

        // 4. Запись контента отчета
        PrintWriter writer = response.getWriter();

        writer.println("==================================================================");
        writer.println("             КОМПЛЕКСНИЙ ФІНАНСОВИЙ ЗВІТ КОРИСТУВАЧА              ");
        writer.println("==================================================================");
        writer.println("Користувач (Email): " + email);
        writer.println("Дата генерації:     " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        writer.println("------------------------------------------------------------------");
        writer.println();

        // Пункт 1 и 2 ТЗ: Рахунки
        writer.println("1. ЗАГАЛЬНА СТАТИСТИКА ТА ЗВІТ ПО РАХУНКАХ");
        writer.println("Кількість відкритих рахунків: " + accounts.size());
        writer.println("Загальна кількість операцій за весь час: " + allTransactions.size());
        writer.println();
        writer.println("Список рахунків:");
        if (accounts.isEmpty()) {
            writer.println(" - Немає відкритих рахунків.");
        } else {
            for (Account acc : accounts) {
                writer.println(String.format(" · [Рахунок ID: %d] Тип: %-10s | Баланс: %10s %s | Статус: %s",
                        acc.getId(),
                        acc.getType().toUpperCase(),
                        acc.getBalance().toString(),
                        acc.getCurrency(),
                        acc.getStatus()));
            }
        }
        writer.println("------------------------------------------------------------------");
        writer.println();

        // Пункт 3 ТЗ: Місячний фінансовий звіт
        writer.println("2. МІСЯЧНИЙ ФІНАНСОВИЙ ЗВІТ (ПОТОЧНИЙ МІСЯЦЬ)");
        writer.println("Загальний дохід (INCOME/DEPOSIT):   +" + monthlyIncome + " (умовних одиниць)");
        writer.println("Загальні витрати (EXPENSE/WITHDRAW): -" + monthlyExpense + " (умовних одиниць)");
        writer.println("Чистий фінансовий результат за місяць: " + monthlyIncome.subtract(monthlyExpense));
        writer.println("------------------------------------------------------------------");
        writer.println();

        // Пункт 4 ТЗ: Повна історія транзакцій (ТУТ БЫЛА ОШИБКА, ТЕПЕРЬ ВСЁ ОК)
        writer.println("3. ПОВНА ІСТОРІЯ ТРАНЗАКЦІЙ У ФАЙЛ");
        writer.println(String.format("%-6s | %-19s | %-12s | %-10s | %-6s | %-9s | %s",
                "ID", "Дата й час", "Тип", "Сума", "Валюта", "Статус", "Опис"));
        writer.println("-----------------------------------------------------------------------------------------");

        if (allTransactions.isEmpty()) {
            writer.println("Транзакції відсутні.");
        } else {
            for (Transaction tx : allTransactions) {
                String formattedDate = (tx.getTimestamp() != null)
                        ? tx.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : "—";

                writer.println(String.format("%-6d | %-19s | %-12s | %-10s | %-6s | %-9s | %s",
                        tx.getId(),
                        formattedDate,
                        tx.getType() != null ? tx.getType() : "UNKNOWN",
                        tx.getAmount().toString(),
                        tx.getCurrency() != null ? tx.getCurrency() : "",
                        tx.getStatus() != null ? tx.getStatus() : "",
                        tx.getDescription() != null ? tx.getDescription() : "—"
                ));
            }
        }
        writer.println("==================================================================");

        writer.flush();
        writer.close();
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