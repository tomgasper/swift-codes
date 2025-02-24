package com.tgasper.swiftcodes.service;

import com.tgasper.swiftcodes.dto.CountrySwiftCodesResponse;
import com.tgasper.swiftcodes.dto.SwiftCodeResponse;
import com.tgasper.swiftcodes.dto.request.SwiftCodeRequest;
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
    private final BankRepository bankRepository;

    public SwiftCodeService(SwiftCodeRepository swiftCodeRepository,
                          CountryRepository countryRepository,
                          BankRepository bankRepository) {
        this.swiftCodeRepository = swiftCodeRepository;
        this.countryRepository = countryRepository;
        this.bankRepository = bankRepository;
    }

    public SwiftCodeResponse getSwiftCodeDetails(String inputCode) {
        // swift code cannot be null or empty
        if (inputCode == null || inputCode.trim().isEmpty()) {
            throw new SwiftCodeValidationException("SWIFT code cannot be null or empty");
        }

        // swift code length must be 8 or 11
        if (inputCode.length() != 8 && inputCode.length() != 11) {
            throw new SwiftCodeValidationException("Invalid SWIFT code length");
        }

        // assume headquarter if length is 8 and format to uppercase
        String swiftCode = (inputCode.length() == 8 ? (inputCode + "XXX") : inputCode).toUpperCase();

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

        Country country = countryRepository.findById(request.countryISO2().toUpperCase())
                .orElseGet(() -> {
                    Country newCountry = new Country();
                    newCountry.setIso2Code(request.countryISO2());
                    newCountry.setName(request.countryName());
                    return countryRepository.save(newCountry);
                });

        String baseSwiftCode = request.swiftCode().substring(0, 8).toUpperCase();
        Bank bank = bankRepository.findBySwiftCode(baseSwiftCode)
                .orElseGet(() -> {
                    Bank newBank = new Bank();
                    newBank.setBankName(request.bankName());
                    newBank.setSwiftCode(baseSwiftCode);
                    return bankRepository.save(newBank);
                });

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
    public String deleteSwiftCode(String swiftCode) {
        if (swiftCode == null || swiftCode.trim().isEmpty()) {
            throw new SwiftCodeValidationException("SWIFT code cannot be null or empty");
        }

        String upperSwiftCode = swiftCode.toUpperCase();

        // get count of the swift codes
        long count = swiftCodeRepository.countBySwiftCodeStartingWith(upperSwiftCode);
        if (count == 0) {
            throw new ResourceNotFoundException(
                    String.format("SWIFT code %s not found", swiftCode));
        }
        else if (count == 1) {
            // delete the entry from bank table as well
            bankRepository.deleteBySwiftCode(upperSwiftCode);
        }

        swiftCodeRepository.deleteById(upperSwiftCode);
        return "SWIFT code deleted successfully";
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
    }
}