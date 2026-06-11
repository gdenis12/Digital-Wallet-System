package com.example.digitalwalletsystem.repository;

import com.example.digitalwalletsystem.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.user.id = :userId")
    List<Account> findByUserId(@Param("userId") Long userId);
}