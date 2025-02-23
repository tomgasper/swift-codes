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

import java.io.FileInputStream;
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
    public void parseAndSave(String filePath) throws IOException {
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .build();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(filePath));
             CSVParser csvParser = new CSVParser(reader, csvFormat)) {

            for (CSVRecord record : csvParser) {
                processRecord(record);
            }
        }
    }

    private void processRecord(CSVRecord record) {
        String iso2Code = record.get("COUNTRY ISO2 CODE").toUpperCase();
        String swiftCodeStr = record.get("SWIFT CODE");
        String bankName = record.get("NAME");
        String address = determineAddress(record);
        boolean isHeadquarter = swiftCodeStr.endsWith("XXX");

        // get or create country
        Country country = getOrCreateCountry(iso2Code, record.get("COUNTRY NAME").toUpperCase());

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
        String address = record.get("ADDRESS");
        if (address != null && !address.trim().isEmpty()) {
            return address;
        }
        return String.format("%s, %s",
                record.get("TOWN NAME"),
                record.get("COUNTRY NAME"));
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
        return bankRepository.findBySwiftCode_SwiftCode(baseSwiftCode)
                .orElseGet(() -> {
                    Bank bank = new Bank();
                    bank.setBankName(bankName);
                    return bankRepository.save(bank);
                });
    }
}