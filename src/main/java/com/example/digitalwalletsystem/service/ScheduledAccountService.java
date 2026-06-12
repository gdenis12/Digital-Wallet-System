package com.example.digitalwalletsystem.service;

import com.example.digitalwalletsystem.model.Account;
import com.example.digitalwalletsystem.model.Transaction;
import com.example.digitalwalletsystem.repository.AccountRepository;
import com.example.digitalwalletsystem.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ScheduledAccountService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledAccountService.class);

    private static final BigDecimal SAVINGS_DAILY_RATE = new BigDecimal("0.02");
    private static final BigDecimal MONTHLY_FEE_RATE = new BigDecimal("0.01");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public ScheduledAccountService(AccountRepository accountRepository,
                                   TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // ===================== DAILY INTEREST =====================

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void applySavingsInterest() {

        log.info("[Scheduler] Running daily savings interest accrual");

        List<Account> savingsAccounts =
                accountRepository.findByTypeAndStatus("savings", "ACTIVE");

        for (Account account : savingsAccounts) {

            BigDecimal interest = account.getBalance()
                    .multiply(SAVINGS_DAILY_RATE)
                    .setScale(2, RoundingMode.HALF_UP);

            if (interest.compareTo(BigDecimal.ZERO) <= 0) continue;

            account.setBalance(account.getBalance().add(interest));
            accountRepository.save(account);

            Transaction tx = new Transaction(
                    null,
                    account,
                    interest,
                    account.getCurrency(),
                    "SUCCESS",
                    "Daily savings interest +2%",
                    "other",
                    "INCOME"
            );

            tx.setType("INCOME"); // ✅ FIX CRITICAL

            transactionRepository.save(tx);

            log.info("[Scheduler] Interest applied: account={} +{}",
                    account.getId(), interest);
        }
    }

    // ===================== MONTHLY FEE =====================

    @Scheduled(cron = "0 0 1 1 * *")
    @Transactional
    public void applyMonthlyFee() {

        log.info("[Scheduler] Running monthly maintenance fee");

        List<Account> activeAccounts = accountRepository.findByStatus("ACTIVE");

        for (Account account : activeAccounts) {

            BigDecimal fee = account.getBalance()
                    .multiply(MONTHLY_FEE_RATE)
                    .setScale(2, RoundingMode.HALF_UP);

            if (fee.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal actualFee = fee.min(account.getBalance());

            account.setBalance(account.getBalance().subtract(actualFee));
            accountRepository.save(account);

            Transaction tx = new Transaction(
                    account,
                    null,
                    actualFee,
                    account.getCurrency(),
                    "SUCCESS",
                    "Monthly maintenance fee 1%",
                    "bills",
                    "FEE"
            );

            tx.setType("EXPENSE"); // ✅ FIX CRITICAL

            transactionRepository.save(tx);

            log.info("[Scheduler] Fee charged: account={} -{}",
                    account.getId(), actualFee);
        }
    }
}