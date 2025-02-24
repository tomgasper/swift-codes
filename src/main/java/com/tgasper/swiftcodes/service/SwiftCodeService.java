package com.tgasper.swiftcodes.service;

import com.tgasper.swiftcodes.dto.CountrySwiftCodesResponse;
import com.tgasper.swiftcodes.dto.SwiftCodeResponse;
import com.tgasper.swiftcodes.dto.request.SwiftCodeRequest;
import com.tgasper.swiftcodes.exception.ConflictException;
import com.tgasper.swiftcodes.exception.ResourceNotFoundException;
import com.tgasper.swiftcodes.exception.SwiftCodeValidationException;
import com.tgasper.swiftcodes.model.Bank;
import com.tgasper.swiftcodes.model.Country;
import com.tgasper.swiftcodes.model.SwiftCode;
import com.tgasper.swiftcodes.repository.BankRepository;
import com.tgasper.swiftcodes.repository.CountryRepository;
import com.tgasper.swiftcodes.repository.SwiftCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SwiftCodeService {
    private final SwiftCodeRepository swiftCodeRepository;
    private final CountryRepository countryRepository;
    private final BankService bankService;

    public SwiftCodeService(SwiftCodeRepository swiftCodeRepository,
                          CountryRepository countryRepository,
                          BankRepository bankRepository,
                          BankService bankService) {
        this.swiftCodeRepository = swiftCodeRepository;
        this.countryRepository = countryRepository;
        this.bankService = bankService;
    }

    public SwiftCodeResponse getSwiftCodeDetails(String inputCode) {
        // swift code cannot be null or empty
        if (inputCode == null || inputCode.trim().isEmpty()) {
            throw new SwiftCodeValidationException("SWIFT code cannot be null or empty");
        }

        String swiftCode = getDefaultSwiftCode(inputCode);

        SwiftCode mainCode = swiftCodeRepository.findById(swiftCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("SWIFT code %s not found", swiftCode)));

        if (mainCode.isHeadquarter()) {
            String baseCode = swiftCode.substring(0, 8);
            List<SwiftCode> branches = swiftCodeRepository.findBySwiftCodeStartingWith(baseCode).stream()
                    .filter(code -> !code.getSwiftCode().equals(swiftCode))
                    .collect(Collectors.toList());
            return new SwiftCodeResponse(mainCode, branches);
        }

        return new SwiftCodeResponse(mainCode);
    }

    public CountrySwiftCodesResponse getSwiftCodesByCountry(String countryISO2) {
        if (countryISO2 == null || countryISO2.trim().isEmpty()) {
            throw new SwiftCodeValidationException("Country ISO2 code cannot be null or empty");
        }

        if (!countryISO2.matches("^[A-Za-z]{2}$")) {
            throw new SwiftCodeValidationException("Invalid country code format. Must be exactly 2 letters.");
        }

        String upperCountryISO2 = countryISO2.toUpperCase();
        Country country = countryRepository.findById(upperCountryISO2)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Country with ISO2 code %s not found", countryISO2)));

        List<SwiftCode> swiftCodes = swiftCodeRepository.findByCountryIso2Code(
                upperCountryISO2);

        if (swiftCodes.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format("No SWIFT codes found for country %s", countryISO2));
        }

        return new CountrySwiftCodesResponse(
                country.getIso2Code(),
                country.getName(),
                swiftCodes
        );
    }

    @Transactional
    public String addSwiftCode(SwiftCodeRequest request) {
        validateSwiftCodeRequest(request);

        // check if SWIFT code already exists
        if (swiftCodeRepository.findById(request.swiftCode().toUpperCase()).isPresent()) {
            throw new ConflictException("SWIFT code " + request.swiftCode() + " already exists");
        }

        Country country = countryRepository.findById(request.countryISO2().toUpperCase())
                .orElseGet(() -> {
                    Country newCountry = new Country();
                    newCountry.setIso2Code(request.countryISO2());
                    newCountry.setName(request.countryName());
                    return countryRepository.save(newCountry);
                });

        String baseSwiftCode = request.swiftCode().substring(0, 8).toUpperCase();
        Bank bank = bankService.getOrCreateBank(baseSwiftCode, request.bankName());

        SwiftCode swiftCode = new SwiftCode();
        swiftCode.setSwiftCode(request.swiftCode().toUpperCase());
        swiftCode.setBank(bank);
        swiftCode.setAddress(request.address());
        swiftCode.setHeadquarter(request.isHeadquarter());
        swiftCode.setCountry(country);

        swiftCodeRepository.save(swiftCode);
        return "SWIFT code added successfully";
    }

    @Transactional
    public String deleteSwiftCode(String inputCode) {
        if (inputCode == null || inputCode.trim().isEmpty()) {
            throw new SwiftCodeValidationException("SWIFT code cannot be null or empty");
        }

        String swiftCode = getDefaultSwiftCode(inputCode);

        // get count of the swift codes
        String baseSwiftCode = swiftCode.substring(0, 8);

        // nothing to delete if the swift code does not exist
        if (!swiftCodeRepository.existsById(swiftCode)) {
            throw new ResourceNotFoundException(
                    String.format("SWIFT code %s not found", swiftCode));
        }

        // count the number of swift codes
        long count = swiftCodeRepository.countBySwiftCodeStartingWith(baseSwiftCode);

        // at least one swift code must exist
        if (count == 1) {
            // delete the entry from bank table as well
            swiftCodeRepository.deleteById(swiftCode);
            bankService.deleteBank(baseSwiftCode);
            return "SWIFT code deleted successfully";
        } else {
            // just delete the entry from swift codes table
            swiftCodeRepository.deleteById(swiftCode);
            return "SWIFT code deleted successfully";
        }
    }

    private String getDefaultSwiftCode(String inputSwiftCode) {
        // swift code length must be 8 or 11
        if (inputSwiftCode.length() != 8 && inputSwiftCode.length() != 11) {
            throw new SwiftCodeValidationException("Invalid SWIFT code length");
        }

        // assume headquarter if length is 8 and format to uppercase
        return (inputSwiftCode.length() == 8 ? (inputSwiftCode + "XXX") : inputSwiftCode).toUpperCase();
    }

    private void validateSwiftCodeRequest(SwiftCodeRequest request) {
        if (request == null) {
            throw new SwiftCodeValidationException("Request body cannot be null");
        }
        if (request.swiftCode() == null || !request.swiftCode().matches("^[A-Za-z]{6}[A-Za-z0-9]{2}([A-Za-z0-9]{3})?$")) {
            throw new SwiftCodeValidationException("Invalid SWIFT code format");
        }
        if (request.bankName() == null || request.bankName().trim().isEmpty()) {
            throw new SwiftCodeValidationException("Bank name is required");
        }
        if (request.countryISO2() == null || !request.countryISO2().matches("^[A-Za-z]{2}$")) {
            throw new SwiftCodeValidationException("Invalid country ISO2 code format");
        }
        if (request.countryName() == null || request.countryName().trim().isEmpty()) {
            throw new SwiftCodeValidationException("Country name is required");
        }
        
        boolean hasXXXSuffix = request.swiftCode().endsWith("XXX");
        if (hasXXXSuffix != request.isHeadquarter()) {
            throw new SwiftCodeValidationException("Inconsistent headquarters flag: SWIFT codes ending with 'XXX' must be marked as headquarters and vice versa");
        }
    }
}