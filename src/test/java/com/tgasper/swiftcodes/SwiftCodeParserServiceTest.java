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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
        String csvContent = """
            COUNTRY ISO2 CODE,SWIFT CODE,CODE TYPE,NAME,ADDRESS,TOWN NAME,COUNTRY NAME,TIME ZONE
            US,CITIUS33XXX,1,CITIBANK NA,399 PARK AVENUE,NEW YORK,UNITED STATES,EST
            US,CITIUS33LAX,1,CITIBANK NA,400 SOUTH GRAND AVE,LOS ANGELES,UNITED STATES,PST
            """;

        Country testCountry = new Country();
        testCountry.setIso2Code("US");
        testCountry.setName("UNITED STATES");
        
        Bank testBank = new Bank();
        testBank.setSwiftCode("CITIUS33");
        testBank.setBankName("CITIBANK NA");

        when(countryRepository.findAll()).thenReturn(List.of(testCountry));
        when(bankRepository.findAll()).thenReturn(List.of(testBank));

        Path tempFile = createTempFileWithContent(csvContent);
        swiftCodeParserService.parseAndSave(tempFile.toString());

        verify(swiftCodeRepository).saveAll(argThat(iterable -> {
            List<SwiftCode> list = new ArrayList<>();
            iterable.forEach(list::add);
            return list.size() == 2 && 
                   list.stream().anyMatch(sc -> sc.getSwiftCode().equals("CITIUS33XXX")) &&
                   list.stream().anyMatch(sc -> sc.getSwiftCode().equals("CITIUS33LAX"));
        }));

        Files.deleteIfExists(tempFile);
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
        
        Path tempFile = createTempFileWithContent(csvContent);
        swiftCodeParserService.parseAndSave(tempFile.toString());
        
        verify(swiftCodeRepository).saveAll(argThat(iterable -> {
            List<SwiftCode> list = new ArrayList<>();
            iterable.forEach(list::add);
            return list.size() == 1 && 
                   list.get(0).getSwiftCode().equals("CITIUS33XXX") &&
                   list.get(0).isHeadquarter();
        }));
        
        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldParseValidBranchRow() throws IOException {
        String csvContent = """
            COUNTRY ISO2 CODE,SWIFT CODE,CODE TYPE,NAME,ADDRESS,TOWN NAME,COUNTRY NAME,TIME ZONE
            US,CITIUS33LAX,1,CITIBANK NA,400 GRAND AVE,LOS ANGELES,UNITED STATES,PST
            """;
        
        when(countryRepository.findAll()).thenReturn(List.of());
        when(bankRepository.findAll()).thenReturn(List.of());
        
        Path tempFile = createTempFileWithContent(csvContent);
        swiftCodeParserService.parseAndSave(tempFile.toString());
        
        verify(swiftCodeRepository).saveAll(argThat(iterable -> {
            List<SwiftCode> list = new ArrayList<>();
            iterable.forEach(list::add);
            return list.size() == 1 && 
                   list.get(0).getSwiftCode().equals("CITIUS33LAX") &&
                   !list.get(0).isHeadquarter();
        }));
        
        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldTreat8CharacterCodeAsHeadquarter() throws IOException {
        String csvContent = """
            COUNTRY ISO2 CODE,SWIFT CODE,CODE TYPE,NAME,ADDRESS,TOWN NAME,COUNTRY NAME,TIME ZONE
            US,CITIUS33,1,CITIBANK NA,399 PARK AVENUE,NEW YORK,UNITED STATES,EST
            """;
        
        when(countryRepository.findAll()).thenReturn(List.of());
        when(bankRepository.findAll()).thenReturn(List.of());
        
        Path tempFile = createTempFileWithContent(csvContent);
        swiftCodeParserService.parseAndSave(tempFile.toString());
        
        verify(swiftCodeRepository).saveAll(argThat(iterable -> {
            List<SwiftCode> list = new ArrayList<>();
            iterable.forEach(list::add);
            return list.size() == 1 && 
                   list.get(0).getSwiftCode().equals("CITIUS33XXX") &&
                   list.get(0).isHeadquarter();
        }));
        
        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldNormalizeCountryCode() throws IOException {
        String csvContent = """
            COUNTRY ISO2 CODE,SWIFT CODE,CODE TYPE,NAME,ADDRESS,TOWN NAME,COUNTRY NAME,TIME ZONE
            us,CITIUS33XXX,1,CITIBANK NA,399 PARK AVENUE,NEW YORK,United States,EST
            """;
        
        when(countryRepository.findAll()).thenReturn(List.of());
        when(bankRepository.findAll()).thenReturn(List.of());
        
        Path tempFile = createTempFileWithContent(csvContent);
        swiftCodeParserService.parseAndSave(tempFile.toString());
        
        verify(countryRepository).saveAll(argThat(iterable -> {
            List<Country> list = new ArrayList<>();
            iterable.forEach(list::add);
            return list.size() == 1 && 
                   list.get(0).getIso2Code().equals("US") &&
                   list.get(0).getName().equals("UNITED STATES");
        }));
        
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
        
        when(countryRepository.findAll()).thenReturn(List.of());
        when(bankRepository.findAll()).thenReturn(List.of());
        
        Path tempFile = createTempFileWithContent(csvContent);
        swiftCodeParserService.parseAndSave(tempFile.toString());
        
        verify(swiftCodeRepository).saveAll(argThat(iterable -> {
            List<SwiftCode> list = new ArrayList<>();
            iterable.forEach(list::add);
            return list.size() == 3 &&
                   list.stream().anyMatch(sc -> sc.getAddress().equals("NEW YORK, UNITED STATES")) &&
                   list.stream().anyMatch(sc -> sc.getAddress().equals("1 CHURCHILL PLACE")) &&
                   list.stream().anyMatch(sc -> sc.getAddress().equals("FRANCE"));
        }));
        
        Files.deleteIfExists(tempFile);
    }

    private Path createTempFileWithContent(String content) throws IOException {
        Path tempFile = Files.createTempFile("test-swift-codes", ".csv");
        Files.writeString(tempFile, content);
        return tempFile;
    }
}