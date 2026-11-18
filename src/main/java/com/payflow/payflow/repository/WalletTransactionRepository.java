package com.payflow.payflow.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.payflow.payflow.entity.WalletTransaction;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Optional<WalletTransaction> findByIdempotencyKey(String idempotencyKey);

    @Query("select t from WalletTransaction t " +
           "where t.senderWallet.user.email = :email " +
           "or t.receiverWallet.user.email = :email " +
           "order by t.createdAt desc")
    List<WalletTransaction> findHistoryForEmail(@Param("email") String email);

    List<WalletTransaction> findAllByOrderByCreatedAtDesc();
}
