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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class SwiftCodeParserService {
    private final SwiftCodeRepository swiftCodeRepository;
    private final BankRepository bankRepository;
    private final CountryRepository countryRepository;
    private final BatchProcessor<SwiftCode> batchProcessor;
    
    // Cache for countries and banks to avoid repeated database queries
    private final Map<String, Country> countryCache = new HashMap<>();
    private final Map<String, Bank> bankCache = new HashMap<>();

    private static final String[] HEADERS = {
            "COUNTRY ISO2 CODE", "SWIFT CODE", "CODE TYPE", "NAME",
            "ADDRESS", "TOWN NAME", "COUNTRY NAME", "TIME ZONE"
    };

    public SwiftCodeParserService(SwiftCodeRepository swiftCodeRepository,
                                 BankRepository bankRepository,
                                 CountryRepository countryRepository,
                                 BatchProcessor<SwiftCode> batchProcessor) {
        this.swiftCodeRepository = swiftCodeRepository;
        this.bankRepository = bankRepository;
        this.countryRepository = countryRepository;
        this.batchProcessor = batchProcessor;
    }

    @Transactional
    public void parseAndSave(String filePath) throws IOException {
        // pre-load existing countries and banks
        countryRepository.findAll().forEach(country -> 
            countryCache.put(country.getIso2Code(), country));
        bankRepository.findAll().forEach(bank -> 
            bankCache.put(bank.getSwiftCode(), bank));

        List<Country> newCountries = new ArrayList<>();
        List<Bank> newBanks = new ArrayList<>();

        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .setDelimiter(',')
                .setQuote('"')
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath));
             CSVParser csvParser = new CSVParser(reader, csvFormat)) {

            for (CSVRecord record : csvParser) {
                processRecord(record, newCountries, newBanks);
            }
            
            // flush any remaining records
            batchProcessor.flush(swiftCodeRepository::saveAll);
            
            // save new countries and banks
            if (!newCountries.isEmpty()) {
                countryRepository.saveAll(newCountries);
            }
            if (!newBanks.isEmpty()) {
                bankRepository.saveAll(newBanks);
            }
        }
    }

    private void processRecord(CSVRecord record, List<Country> newCountries, List<Bank> newBanks) {
        String iso2Code = record.get("COUNTRY ISO2 CODE").trim().toUpperCase();
        String swiftCodeStr = record.get("SWIFT CODE").trim();
        String bankName = record.get("NAME").trim();
        String address = determineAddress(record);

        // normalize SWIFT code -> append XXX for 8 char codes
        if (swiftCodeStr.length() == 8) {
            swiftCodeStr = swiftCodeStr + "XXX";
        }
        boolean isHeadquarter = swiftCodeStr.endsWith("XXX");

        // get or create country
        String countryName = record.get("COUNTRY NAME").trim().toUpperCase();
        Country country = getOrCreateCountry(iso2Code, countryName, newCountries);

        // get or create bank with base SWIFT code (first 8 characters)
        String baseSwiftCode = swiftCodeStr.substring(0, 8);
        Bank bank = getOrCreateBank(baseSwiftCode, bankName, newBanks);

        // create new SwiftCode entity
        SwiftCode swiftCode = new SwiftCode();
        swiftCode.setSwiftCode(swiftCodeStr);
        swiftCode.setBank(bank);
        swiftCode.setAddress(address);
        swiftCode.setHeadquarter(isHeadquarter);
        swiftCode.setCountry(country);

        batchProcessor.add(swiftCode, swiftCodeRepository::saveAll);
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

    private Country getOrCreateCountry(String iso2Code, String countryName, List<Country> newCountries) {
        return countryCache.computeIfAbsent(iso2Code, k -> {
            Country country = new Country();
            country.setIso2Code(iso2Code);
            country.setName(countryName);
            newCountries.add(country);
            return country;
        });
    }

    private Bank getOrCreateBank(String baseSwiftCode, String bankName, List<Bank> newBanks) {
        return bankCache.computeIfAbsent(baseSwiftCode, k -> {
            Bank bank = new Bank();
            bank.setBankName(bankName);
            bank.setSwiftCode(baseSwiftCode);
            newBanks.add(bank);
            return bank;
        });
    }
}