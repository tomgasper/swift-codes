package com.tgasper.swiftcodes.repository;

import com.tgasper.swiftcodes.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, String> {
}