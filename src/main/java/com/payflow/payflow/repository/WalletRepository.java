package com.payflow.payflow.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.payflow.payflow.entity.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser_Email(String email);
    boolean existsByUser_Email(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") Long id);

    @Query("select w.id from Wallet w where w.user.email = :email")
    Optional<Long> findWalletIdByEmail(@Param("email") String email);

    @Query("select w.id from Wallet w where w.user.email = :email")
    Optional<Long> findWalletIdByUserEmail(@Param("email") String email);
}
