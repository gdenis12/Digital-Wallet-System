package com.example.digitalwalletsystem.service;

import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.model.Transaction;
import com.example.digitalwalletsystem.repository.AccountRepository;
import com.example.digitalwalletsystem.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    // Deposit
    @Transactional
    public Transaction deposit(Long toAccountId, BigDecimal amount, String description) {
        Account account = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if ("BLOCKED".equals(account.getStatus())) {
            throw new RuntimeException("The operation is not possible: the account is blocked!");
        }


        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);


        Transaction transaction = new Transaction(null, account, amount, account.getCurrency(), "SUCCESS", description, "other");
        return transactionRepository.save(transaction);
    }

    // Withdraw
    @Transactional
    public Transaction withdraw(Long fromAccountId, BigDecimal amount, String description, String category) {
        Account account = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if ("BLOCKED".equals(account.getStatus())) {
            throw new RuntimeException("The operation is not possible: the account is blocked!");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("There are insufficient funds in the account");
        }


        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);


        Transaction transaction = new Transaction(account, null, amount, account.getCurrency(), "SUCCESS", description, category);
        return transactionRepository.save(transaction);
    }

    // Transfer
    @Transactional
    public Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description, String category) {
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        if ("BLOCKED".equals(fromAccount.getStatus()) || "BLOCKED".equals(toAccount.getStatus())) {
            throw new RuntimeException("Transaction declined: one of the accounts is blocked!");
        }


        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new RuntimeException("Currency conversion is not yet supported! Account currencies must match.");
        }


        BigDecimal feePercent = new BigDecimal("0.02");
        BigDecimal fee = amount.multiply(feePercent);
        BigDecimal totalDeduct = amount.add(fee);

        if (fromAccount.getBalance().compareTo(totalDeduct) < 0) {
            throw new RuntimeException("Insufficient funds for transfer and payment of commission (2%)");
        }


        fromAccount.setBalance(fromAccount.getBalance().subtract(totalDeduct));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);


        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            Transaction feeTx = new Transaction(fromAccount, null, fee, fromAccount.getCurrency(), "SUCCESS", "Transfer fee 2%", "bills");
            transactionRepository.save(feeTx);
        }


        Transaction transaction = new Transaction(fromAccount, toAccount, amount, fromAccount.getCurrency(), "SUCCESS", description, category);
        return transactionRepository.save(transaction);
    }

    // History account transactions
    public List<Transaction> getAccountHistory(Long accountId) {
        return transactionRepository.findHistoryByAccountId(accountId);
    }

    // Global journal
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}