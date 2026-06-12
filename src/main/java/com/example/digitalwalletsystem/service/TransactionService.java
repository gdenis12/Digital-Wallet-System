package com.example.digitalwalletsystem.service;

import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.model.Transaction;
import com.example.digitalwalletsystem.repository.AccountRepository;
import com.example.digitalwalletsystem.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    public List<Transaction> getFilteredHistory(Long userId, Long accountId, String type, String category, LocalDate from, LocalDate to) {
        LocalDateTime fromDate = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDate = (to != null) ? to.atTime(LocalTime.MAX) : null;

        String cleanType = (type != null && !type.isBlank()) ? type : null;
        String cleanCategory = (category != null && !category.isBlank()) ? category : null;


        return transactionRepository.findFilteredHistory(userId, accountId, cleanType, cleanCategory, fromDate, toDate);
    }



    @Transactional
    public Transaction deposit(Long toAccountId, BigDecimal amount, String description) {
        Account account = accountRepository.findById(toAccountId).orElseThrow(() -> new RuntimeException("Account not found"));
        if ("BLOCKED".equals(account.getStatus())) throw new RuntimeException("Account is blocked");
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction(null, account, amount, account.getCurrency(), "SUCCESS", description, "deposit", "INCOME");
        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction withdraw(Long fromAccountId, BigDecimal amount, String description, String category) {
        Account account = accountRepository.findById(fromAccountId).orElseThrow(() -> new RuntimeException("Account not found"));
        if ("BLOCKED".equals(account.getStatus())) throw new RuntimeException("Account is blocked");
        if (account.getBalance().compareTo(amount) < 0) throw new RuntimeException("Insufficient funds");
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction(account, null, amount, account.getCurrency(), "SUCCESS", description, category, "EXPENSE");
        return transactionRepository.save(tx);
    }

    @Transactional
    public Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description, String category) {
        Account from = accountRepository.findById(fromAccountId).orElseThrow(() -> new RuntimeException("Sender account not found"));
        Account to = accountRepository.findById(toAccountId).orElseThrow(() -> new RuntimeException("Recipient account not found"));
        if ("BLOCKED".equals(from.getStatus()) || "BLOCKED".equals(to.getStatus())) throw new RuntimeException("Account blocked");
        if (!from.getCurrency().equals(to.getCurrency())) throw new RuntimeException("Currency mismatch");

        BigDecimal fee = amount.multiply(new BigDecimal("0.02"));
        BigDecimal total = amount.add(fee);
        if (from.getBalance().compareTo(total) < 0) throw new RuntimeException("Insufficient funds");

        from.setBalance(from.getBalance().subtract(total));
        to.setBalance(to.getBalance().add(amount));
        accountRepository.save(from);
        accountRepository.save(to);

        Transaction tx = new Transaction(from, to, amount, from.getCurrency(), "SUCCESS", description, category, "TRANSFER");
        return transactionRepository.save(tx);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}