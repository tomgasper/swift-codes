package com.tgasper.swiftcodes.repository;

import com.tgasper.swiftcodes.model.SwiftCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SwiftCodeRepository extends JpaRepository<SwiftCode, String> {
} 