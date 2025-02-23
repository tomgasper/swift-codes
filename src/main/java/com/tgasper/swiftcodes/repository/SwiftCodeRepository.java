package com.tgasper.swiftcodes.repository;

import com.tgasper.swiftcodes.model.SwiftCode;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SwiftCodeRepository extends JpaRepository<SwiftCode, String> {
    List<SwiftCode> findBySwiftCodeStartingWith(String baseCode);
    List<SwiftCode> findByCountryIso2Code(String countryIso2Code, Sort sort);
} 