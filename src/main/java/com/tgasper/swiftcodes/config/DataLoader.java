package com.tgasper.swiftcodes.config;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.tgasper.swiftcodes.service.SwiftCodeParserService;

@Component
public class DataLoader implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private final SwiftCodeParserService swiftCodeParserService;
    private final ApplicationArgumentsConfig argumentsConfig;

    @Autowired
    public DataLoader(SwiftCodeParserService swiftCodeParserService, 
                     ApplicationArgumentsConfig argumentsConfig) {
        this.swiftCodeParserService = swiftCodeParserService;
        this.argumentsConfig = argumentsConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        if (argumentsConfig.isServerMode()) {
            logger.info("Starting in server-only mode...");
            return;
        }

        // import mode is default or explicitly specified
        importData();
    }

    private void importData() {
        try {
            String importPath = argumentsConfig.getImportPath();
            Path csvPath;

            if (importPath != null && !importPath.isEmpty()) {
                // use provided path
                csvPath = Paths.get(importPath);
                if (!Files.exists(csvPath)) {
                    throw new IOException("File not found: " + importPath);
                }
            } else {
                // sse default path from resources
                URL resource = getClass().getResource("/swift-codes.csv");
                if (resource == null) {
                    throw new IOException("Could not find swift-codes.csv in resources");
                }
                csvPath = Paths.get(resource.toURI());
            }

            logger.info("Importing data from: {}", csvPath);
            swiftCodeParserService.parseAndSave(csvPath.toString());
            logger.info("Data import completed successfully");
        } catch (IOException | URISyntaxException e) {
            logger.error("Error during data import: {}", e.getMessage(), e);
        }
    }
}