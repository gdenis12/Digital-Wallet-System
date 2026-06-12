package com.example.digitalwalletsystem.repository;

import com.example.digitalwalletsystem.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.user u WHERE :userId IS NULL OR u.id = :userId")
    List<Account> findByUserId(@Param("userId") Long userId);

    List<Account> findByStatus(String status);
    List<Account> findByTypeAndStatus(String type, String status);
}