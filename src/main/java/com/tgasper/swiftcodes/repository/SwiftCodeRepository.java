package com.tgasper.swiftcodes.repository;

import com.tgasper.swiftcodes.model.SwiftCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SwiftCodeRepository extends JpaRepository<SwiftCode, String> {
    List<SwiftCode> findBySwiftCodeStartingWith(String baseCode);
    @Query("SELECT s FROM SwiftCode s JOIN FETCH s.bank WHERE s.country.iso2Code = :countryIso2Code ORDER BY s.swiftCode")
    List<SwiftCode> findByCountryIso2Code(@Param("countryIso2Code") String countryIso2Code);
} 