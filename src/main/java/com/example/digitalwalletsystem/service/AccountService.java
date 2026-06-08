package com.example.digitalwalletsystem.service;

import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // create account
    public Account createAccount(Account account) {

        account.setBalance(BigDecimal.ZERO);
        account.setStatus("ACTIVE");

        return accountRepository.save(account);
    }

    // user accounts
    public List<Account> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    // account balance
    public BigDecimal getBalance(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return account.getBalance();
    }

    // deposit funds
    public Account deposit(Long accountId, BigDecimal amount) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance().add(amount));

        return accountRepository.save(account);
    }

    // withdraw funds
    public Account withdraw(Long accountId, BigDecimal amount) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));

        return accountRepository.save(account);
    }

    // block account
    public Account blockAccount(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setStatus("BLOCKED");

        return accountRepository.save(account);
    }
}