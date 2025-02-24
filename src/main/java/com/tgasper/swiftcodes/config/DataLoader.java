package com.tgasper.swiftcodes.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
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
                // Use default path from resources
                csvPath = loadDefaultResourceFile();
            }

            logger.info("Importing data from: {}", csvPath);
            swiftCodeParserService.parseAndSave(csvPath.toString());
            logger.info("Data import completed successfully");
            logger.info("Started server...");
        } catch (IOException e) {
            logger.error("Error during data import: {}", e.getMessage(), e);
        }
    }

    private Path loadDefaultResourceFile() throws IOException {
        try {
            // create a temporary file
            Path tempFile = Files.createTempFile("swift-codes", ".csv");
            
            // copy the resource file content to the temporary file
            try (InputStream is = new ClassPathResource("swift-codes.csv").getInputStream()) {
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // delete the temporary file when the JVM exits
            tempFile.toFile().deleteOnExit();
            
            return tempFile;
        } catch (IOException e) {
            throw new IOException("Could not load default swift-codes.csv from resources", e);
        }
    }
}