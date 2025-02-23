package com.tgasper.swiftcodes.service;

import com.tgasper.swiftcodes.model.Bank;
import com.tgasper.swiftcodes.model.Country;
import com.tgasper.swiftcodes.model.SwiftCode;
import com.tgasper.swiftcodes.repository.BankRepository;
import com.tgasper.swiftcodes.repository.CountryRepository;
import com.tgasper.swiftcodes.repository.SwiftCodeRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class SwiftCodeParserService {
    private final SwiftCodeRepository swiftCodeRepository;
    private final BankRepository bankRepository;
    private final CountryRepository countryRepository;

    private static final String[] HEADERS = {
            "COUNTRY ISO2 CODE", "SWIFT CODE", "CODE TYPE", "NAME",
            "ADDRESS", "TOWN NAME", "COUNTRY NAME", "TIME ZONE"
    };

    public SwiftCodeParserService(SwiftCodeRepository swiftCodeRepository,
                                 BankRepository bankRepository,
                                 CountryRepository countryRepository) {
        this.swiftCodeRepository = swiftCodeRepository;
        this.bankRepository = bankRepository;
        this.countryRepository = countryRepository;
    }

    @Transactional
    public void parseAndSave(String resourcePath) throws IOException {
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .setDelimiter(',')
                .setQuote('"')
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        try (InputStreamReader reader = new InputStreamReader(
                getClass().getResourceAsStream(resourcePath));
             CSVParser csvParser = new CSVParser(reader, csvFormat)) {

            for (CSVRecord record : csvParser) {
                processRecord(record);
            }
        }
    }

    private void processRecord(CSVRecord record) {
        String iso2Code = record.get("COUNTRY ISO2 CODE").trim().toUpperCase();
        String swiftCodeStr = record.get("SWIFT CODE").trim();
        String bankName = record.get("NAME").trim();
        String address = determineAddress(record);
        boolean isHeadquarter = swiftCodeStr.endsWith("XXX");

        // get or create country
        String countryName = record.get("COUNTRY NAME").trim().toUpperCase();
        Country country = getOrCreateCountry(iso2Code, countryName);

        // get or create bank with base SWIFT code (first 8 characters)
        String baseSwiftCode = swiftCodeStr.substring(0, 8);
        Bank bank = getOrCreateBank(baseSwiftCode, bankName);

        // create new SwiftCode entity
        SwiftCode swiftCode = new SwiftCode();
        swiftCode.setSwiftCode(swiftCodeStr);
        swiftCode.setBank(bank);
        swiftCode.setAddress(address);
        swiftCode.setHeadquarter(isHeadquarter);
        swiftCode.setCountry(country);

        swiftCodeRepository.save(swiftCode);
    }

    private String determineAddress(CSVRecord record) {
        String address = record.get("ADDRESS").trim();
        String townName = record.get("TOWN NAME").trim();
        String countryName = record.get("COUNTRY NAME").trim();

        if (address != null && !address.isEmpty()) {
            return address;
        }
        
        if (!townName.isEmpty()) {
            return String.format("%s, %s", townName, countryName);
        }
        
        return countryName;
    }

    private Country getOrCreateCountry(String iso2Code, String countryName) {
        return countryRepository.findById(iso2Code)
                .orElseGet(() -> {
                    Country country = new Country();
                    country.setIso2Code(iso2Code);
                    country.setName(countryName);
                    return countryRepository.save(country);
                });
    }

    private Bank getOrCreateBank(String baseSwiftCode, String bankName) {
        return bankRepository.findBySwiftCode(baseSwiftCode)
                .orElseGet(() -> {
                    Bank bank = new Bank();
                    bank.setBankName(bankName);
                    bank.setSwiftCode(baseSwiftCode);
                    return bankRepository.save(bank);
                });
    }
}