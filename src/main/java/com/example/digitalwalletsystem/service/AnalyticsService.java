package com.example.digitalwalletsystem.service;

import com.example.digitalwalletsystem.dto.CategoryStatDto;
import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.model.Transaction;
import com.example.digitalwalletsystem.repository.AccountRepository;
import com.example.digitalwalletsystem.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public AnalyticsService(TransactionRepository transactionRepository,
                            AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    // =========================
    // FETCH TRANSACTIONS
    // =========================
    public List<Transaction> getTransactions(Long userId, Long accountId, LocalDate from, LocalDate to) {

        List<Account> accounts = accountRepository.findByUserId(userId);

        List<Long> accountIds = accounts.stream()
                .map(Account::getId)
                .filter(id -> accountId == null || id.equals(accountId))
                .toList();

        Set<Transaction> all = new HashSet<>();

        for (Long id : accountIds) {
            all.addAll(transactionRepository.findHistoryByAccountId(id));
        }

        return all.stream()
                .filter(t -> {
                    LocalDate date = t.getTimestamp().toLocalDate();
                    return !date.isBefore(from) && !date.isAfter(to);
                })
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .toList();
    }

    // =========================
    // TYPE HELPERS (ВАЖНО)
    // =========================
    private boolean isFee(Transaction t) {
        return "FEE".equalsIgnoreCase(t.getType());
    }

    private boolean isIncome(Transaction t) {
        return t != null
                && "INCOME".equalsIgnoreCase(t.getType())
                && !isFee(t);
    }

    private boolean isExpense(Transaction t) {
        return t != null
                && (
                "EXPENSE".equalsIgnoreCase(t.getType())
                        || "TRANSFER".equalsIgnoreCase(t.getType())
        )
                && !isFee(t);
    }

    // =========================
    // STATS
    // =========================

    public BigDecimal getTotalSpent(List<Transaction> txs) {
        return txs.stream()
                .filter(this::isExpense)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalIncome(List<Transaction> txs) {
        return txs.stream()
                .filter(this::isIncome)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getAvg(List<Transaction> txs) {
        if (txs.isEmpty()) return BigDecimal.ZERO;

        BigDecimal sum = txs.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(txs.size()), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getLargest(List<Transaction> txs) {
        return txs.stream()
                .map(Transaction::getAmount)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    public List<Transaction> getTop5(List<Transaction> txs) {
        return txs.stream()
                .filter(this::isExpense)
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .limit(5)
                .toList();
    }

    // =========================
    // CATEGORY STATS (FEE IGNORE)
    // =========================
    public List<CategoryStatDto> getCategoryStats(List<Transaction> txs) {

        Map<String, CategoryStatDto> map = new LinkedHashMap<>();

        for (Transaction t : txs) {

            if (isFee(t)) continue;

            String cat = (t.getCategory() != null && !t.getCategory().isBlank())
                    ? t.getCategory()
                    : "other";

            map.computeIfAbsent(cat,
                    k -> new CategoryStatDto(k, BigDecimal.ZERO, 0)
            );

            CategoryStatDto stat = map.get(cat);

            stat.setTotal(stat.getTotal().add(t.getAmount()));
            stat.setCount(stat.getCount() + 1);
        }

        return map.values().stream()
                .sorted(Comparator.comparing(CategoryStatDto::getTotal).reversed())
                .toList();
    }

    public List<String> getCategoryLabels(List<Transaction> txs) {
        return getCategoryStats(txs).stream()
                .map(CategoryStatDto::getCategory)
                .toList();
    }

    public List<BigDecimal> getCategoryAmounts(List<Transaction> txs) {
        return getCategoryStats(txs).stream()
                .map(CategoryStatDto::getTotal)
                .toList();
    }

    // =========================
    // MONTH STATS (EXPENSE ONLY)
    // =========================
    public List<String> getMonthLabels(LocalDate from, LocalDate to) {

        List<String> labels = new ArrayList<>();
        YearMonth cur = YearMonth.from(from);
        YearMonth end = YearMonth.from(to);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");

        while (!cur.isAfter(end)) {
            labels.add(cur.atDay(1).format(fmt));
            cur = cur.plusMonths(1);
        }

        return labels;
    }

    public List<BigDecimal> getMonthAmounts(List<Transaction> txs, LocalDate from, LocalDate to) {

        List<BigDecimal> amounts = new ArrayList<>();
        YearMonth cur = YearMonth.from(from);
        YearMonth end = YearMonth.from(to);

        while (!cur.isAfter(end)) {

            YearMonth month = cur;

            BigDecimal sum = txs.stream()
                    .filter(this::isExpense)
                    .filter(t -> YearMonth.from(t.getTimestamp()).equals(month))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            amounts.add(sum);
            cur = cur.plusMonths(1);
        }

        return amounts;
    }
}