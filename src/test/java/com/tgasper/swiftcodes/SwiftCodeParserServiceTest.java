package com.tgasper.swiftcodes;

import com.tgasper.swiftcodes.model.Bank;
import com.tgasper.swiftcodes.model.Country;
import com.tgasper.swiftcodes.model.SwiftCode;
import com.tgasper.swiftcodes.repository.BankRepository;
import com.tgasper.swiftcodes.repository.CountryRepository;
import com.tgasper.swiftcodes.repository.SwiftCodeRepository;
import com.tgasper.swiftcodes.service.SwiftCodeParserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwiftCodeParserServiceTest {
    @Mock
    private SwiftCodeRepository swiftCodeRepository;
    @Mock
    private BankRepository bankRepository;
    @Mock
    private CountryRepository countryRepository;
    @InjectMocks
    private SwiftCodeParserService swiftCodeParserService;

    @Test
    void shouldParseAndSaveValidCSVFile() throws IOException {
        // test CSV content
        String csvContent = """
            COUNTRY ISO2 CODE,SWIFT CODE,CODE TYPE,NAME,ADDRESS,TOWN NAME,COUNTRY NAME,TIME ZONE
            US,CITIUS33XXX,1,CITIBANK NA,399 PARK AVENUE,NEW YORK,UNITED STATES,EST
            US,CITIUS33LAX,1,CITIBANK NA,400 SOUTH GRAND AVE,LOS ANGELES,UNITED STATES,PST
            """;

        // mock repository responses
        Country testCountry = new Country();
        testCountry.setIso2Code("US");
        testCountry.setName("UNITED STATES");
        
        Bank testBank = new Bank();
        testBank.setSwiftCode("CITIUS33");
        testBank.setBankName("CITIBANK NA");

        when(countryRepository.findById("US")).thenReturn(Optional.of(testCountry));
        when(bankRepository.findBySwiftCode("CITIUS33")).thenReturn(Optional.of(testBank));

        Path tempFile = null;
        try {
            // create a temporary file with test content
            tempFile = Files.createTempFile("test-swift-codes", ".csv");
            Files.write(tempFile, csvContent.getBytes());

            swiftCodeParserService.parseAndSave(tempFile.toAbsolutePath().toString());

            verify(swiftCodeRepository, times(2)).save(any(SwiftCode.class));
            verify(countryRepository, times(2)).findById("US");
            verify(bankRepository, times(2)).findBySwiftCode("CITIUS33");
        } finally {
            // clean up the temporary file
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldHandleEmptyFile() {
        String emptyFile = "";
        assertThrows(IOException.class, () -> 
            swiftCodeParserService.parseAndSave(emptyFile));
    }

    @Test
    void shouldHandleInvalidCSVFormat() {
        String invalidCsv = "invalid,csv,format";
        assertThrows(IOException.class, () -> 
            swiftCodeParserService.parseAndSave(invalidCsv));
    }

    @Test
    void shouldParseValidHeadquarterRow() throws IOException {
        // test CSV with HQ(XXX) SWIFT code
        String csvContent = """
            COUNTRY ISO2 CODE,SWIFT CODE,CODE TYPE,NAME,ADDRESS,TOWN NAME,COUNTRY NAME,TIME ZONE
            US,CITIUS33XXX,1,CITIBANK NA,399 PARK AVENUE,NEW YORK,UNITED STATES,EST
            """;
        
        // Setup mocks
        when(countryRepository.findById("US")).thenReturn(Optional.empty());
        when(bankRepository.findBySwiftCode("CITIUS33")).thenReturn(Optional.empty());
        
        // Execute test
        Path tempFile = createTempFileWithContent(csvContent);
        swiftCodeParserService.parseAndSave(tempFile.toString());
        
        // Verify
        verify(swiftCodeRepository).save(argThat(swiftCode -> 
            swiftCode.getSwiftCode().equals("CITIUS33XXX") &&
            swiftCode.isHeadquarter()
        ));
        
        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldParseValidBranchRow() throws IOException {
        // test CSV with branch (non-XXX) SWIFT code
        String csvContent = """
            COUNTRY ISO2 CODE,SWIFT CODE,CODE TYPE,NAME,ADDRESS,TOWN NAME,COUNTRY NAME,TIME ZONE
            US,CITIUS33LAX,1,CITIBANK NA,400 GRAND AVE,LOS ANGELES,UNITED STATES,PST
            """;
        
        when(countryRepository.findById("US")).thenReturn(Optional.empty());
        when(bankRepository.findBySwiftCode("CITIUS33")).thenReturn(Optional.empty());
        
        Path tempFile = createTempFileWithContent(csvContent);
        swiftCodeParserService.parseAndSave(tempFile.toString());
        
        verify(swiftCodeRepository).save(argThat(swiftCode -> 
            swiftCode.getSwiftCode().equals("CITIUS33LAX") &&
            !swiftCode.isHeadquarter()
        ));
        
        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldTreat8CharacterCodeAsHeadquarter() throws IOException {
        String csvContent = """
            COUNTRY ISO2 CODE,SWIFT CODE,CODE TYPE,NAME,ADDRESS,TOWN NAME,COUNTRY NAME,TIME ZONE
            US,CITIUS33,1,CITIBANK NA,399 PARK AVENUE,NEW YORK,UNITED STATES,EST
            """;
        
        when(countryRepository.findById("US")).thenReturn(Optional.empty());
        when(bankRepository.findBySwiftCode("CITIUS33")).thenReturn(Optional.empty());
        
        Path tempFile = createTempFileWithContent(csvContent);
        swiftCodeParserService.parseAndSave(tempFile.toString());
        
        verify(swiftCodeRepository).save(argThat(swiftCode -> 
            swiftCode.getSwiftCode().equals("CITIUS33XXX") &&
            swiftCode.isHeadquarter()
        ));
        
        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldNormalizeCountryCode() throws IOException {
        String csvContent = """
            COUNTRY ISO2 CODE,SWIFT CODE,CODE TYPE,NAME,ADDRESS,TOWN NAME,COUNTRY NAME,TIME ZONE
            us,CITIUS33XXX,1,CITIBANK NA,399 PARK AVENUE,NEW YORK,United States,EST
            """;
        
        when(countryRepository.findById("US")).thenReturn(Optional.empty());
        when(bankRepository.findBySwiftCode("CITIUS33")).thenReturn(Optional.empty());
        
        Path tempFile = createTempFileWithContent(csvContent);
        swiftCodeParserService.parseAndSave(tempFile.toString());
        
        verify(countryRepository).save(argThat(country -> 
            country.getIso2Code().equals("US") &&
            country.getName().equals("UNITED STATES")
        ));
        
        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldHandleAddressVariations() throws IOException {
        String csvContent = """
            COUNTRY ISO2 CODE,SWIFT CODE,CODE TYPE,NAME,ADDRESS,TOWN NAME,COUNTRY NAME,TIME ZONE
            US,CITIUS33XXX,1,CITIBANK NA,,NEW YORK,UNITED STATES,EST
            GB,BARCGB22XXX,1,BARCLAYS,1 CHURCHILL PLACE,,UNITED KINGDOM,GMT
            FR,BNPAFR21,1,BNP PARIBAS,,,FRANCE,CET
            """;
        
        when(countryRepository.findById(anyString())).thenReturn(Optional.empty());
        when(bankRepository.findBySwiftCode(anyString())).thenReturn(Optional.empty());
        
        Path tempFile = createTempFileWithContent(csvContent);
        swiftCodeParserService.parseAndSave(tempFile.toString());
        
        verify(swiftCodeRepository).save(argThat(swiftCode -> 
            swiftCode.getAddress().equals("NEW YORK, UNITED STATES")
        ));
        verify(swiftCodeRepository).save(argThat(swiftCode -> 
            swiftCode.getAddress().equals("1 CHURCHILL PLACE")
        ));
        verify(swiftCodeRepository).save(argThat(swiftCode -> 
            swiftCode.getAddress().equals("FRANCE")
        ));
        
        Files.deleteIfExists(tempFile);
    }

    private Path createTempFileWithContent(String content) throws IOException {
        Path tempFile = Files.createTempFile("test-swift-codes", ".csv");
        Files.writeString(tempFile, content);
        return tempFile;
    }
}