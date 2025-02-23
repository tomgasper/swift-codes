package com.tgasper.swiftcodes.service;

import com.tgasper.swiftcodes.dto.SwiftCodeDetails;
import com.tgasper.swiftcodes.exception.ResourceNotFoundException;
import com.tgasper.swiftcodes.exception.SwiftCodeValidationException;
import com.tgasper.swiftcodes.model.SwiftCode;
import com.tgasper.swiftcodes.repository.SwiftCodeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SwiftCodeService {
    private final SwiftCodeRepository swiftCodeRepository;

    public SwiftCodeService(SwiftCodeRepository swiftCodeRepository) {
        this.swiftCodeRepository = swiftCodeRepository;
    }

    public SwiftCodeDetails getSwiftCodeDetails(String swiftCode) {
        if (swiftCode == null || swiftCode.trim().isEmpty()) {
            throw new SwiftCodeValidationException("SWIFT code cannot be null or empty");
        }

        SwiftCode mainCode = swiftCodeRepository.findById(swiftCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("SWIFT code %s not found", swiftCode)));

        List<SwiftCode> branches = List.of();
        if (mainCode.isHeadquarter()) {
            String baseCode = swiftCode.substring(0, 8);
            branches = swiftCodeRepository.findBySwiftCodeStartingWith(baseCode).stream()
                    .filter(code -> !code.getSwiftCode().equals(swiftCode))
                    .collect(Collectors.toList());
        }

        return new SwiftCodeDetails(mainCode, branches);
    }

    public List<SwiftCode> getSwiftCodesByCountry(String countryISO2) {
        if (countryISO2 == null || countryISO2.trim().isEmpty()) {
            throw new SwiftCodeValidationException("Country ISO2 code cannot be null or empty");
        }

        List<SwiftCode> swiftCodes = swiftCodeRepository.findByCountryIso2Code(
                countryISO2.toUpperCase(),
                Sort.by("swiftCode"));

        if (swiftCodes.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format("No SWIFT codes found for country %s", countryISO2));
        }

        return swiftCodes;
    }

    @Transactional
    public SwiftCode addSwiftCode(SwiftCode swiftCode) {
        validateSwiftCode(swiftCode);

        String swiftCodeStr = swiftCode.getSwiftCode().toUpperCase();
        if (swiftCodeRepository.existsById(swiftCodeStr)) {
            throw new SwiftCodeValidationException(
                    String.format("SWIFT code %s already exists", swiftCodeStr));
        }

        swiftCode.setSwiftCode(swiftCodeStr);
        swiftCode.setHeadquarter(swiftCodeStr.endsWith("XXX"));
        return swiftCodeRepository.save(swiftCode);
    }

    @Transactional
    public void deleteSwiftCode(String swiftCode) {
        if (swiftCode == null || swiftCode.trim().isEmpty()) {
            throw new SwiftCodeValidationException("SWIFT code cannot be null or empty");
        }

        if (!swiftCodeRepository.existsById(swiftCode.toUpperCase())) {
            throw new ResourceNotFoundException(
                    String.format("SWIFT code %s not found", swiftCode));
        }

        swiftCodeRepository.deleteById(swiftCode.toUpperCase());
    }

    private void validateSwiftCode(SwiftCode swiftCode) {
        if (swiftCode == null) {
            throw new SwiftCodeValidationException("SwiftCode object cannot be null");
        }
        if (swiftCode.getSwiftCode() == null || !swiftCode.getSwiftCode().matches("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$")) {
            throw new SwiftCodeValidationException("Invalid SWIFT code format");
        }
        if (swiftCode.getBank() == null) {
            throw new SwiftCodeValidationException("Bank information is required");
        }
        if (swiftCode.getCountry() == null) {
            throw new SwiftCodeValidationException("Country information is required");
        }
    }
}