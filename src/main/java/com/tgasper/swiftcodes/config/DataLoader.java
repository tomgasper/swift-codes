package com.tgasper.swiftcodes.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.tgasper.swiftcodes.service.SwiftCodeParserService;

@Component
public class DataLoader implements CommandLineRunner {
    private final SwiftCodeParserService swiftCodeParserService;

    @Autowired
    public DataLoader(SwiftCodeParserService swiftCodeParserService) {
        this.swiftCodeParserService = swiftCodeParserService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            swiftCodeParserService.parseAndSave("/swift-codes.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}