package com.tgasper.swiftcodes.config;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URISyntaxException;

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
            URL resource = getClass().getResource("/swift-codes.csv");
            if (resource == null) {
                throw new IOException("Could not find swift-codes.csv in resources");
            }
            Path csvPath = Paths.get(resource.toURI());
            swiftCodeParserService.parseAndSave(csvPath.toString());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}