package com.tgasper.swiftcodes.repository;

import com.tgasper.swiftcodes.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional<Bank> findBySwiftCode(String swiftCode);
} 