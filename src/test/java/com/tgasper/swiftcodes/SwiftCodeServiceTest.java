package com.tgasper.swiftcodes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.tgasper.swiftcodes.service.SwiftCodeService;

@ExtendWith(MockitoExtension.class)
class SwiftCodeServiceTest {
    @Mock
    private SwiftCodeRepository swiftCodeRepository;
    @Mock
    private BankRepository bankRepository;
    @Mock
    private CountryRepository countryRepository;
    @InjectMocks
    private SwiftCodeService swiftCodeService;

    @Test
    void shouldGetSwiftCodeDetails() {
        // arrange
        SwiftCode mainCode = createTestSwiftCode("CITIUS12XXX", true);
        when(swiftCodeRepository.findById("CITIUS12XXX"))
            .thenReturn(Optional.of(mainCode));

        // act
        SwiftCodeResponse response = swiftCodeService.getSwiftCodeDetails("CITIUS12XXX");

        // assert
        assertNotNull(response);
        assertEquals("CITIUS12XXX", response.getSwiftCode());
        assertTrue(response.isHeadquarter());
    }

    @Test
    void shouldGetHeadquarterWithBranches() {
        // arrange
        SwiftCode hq = createTestSwiftCode("CITIUS12XXX", true);
        SwiftCode branch = createTestSwiftCode("CITIUS12LAX", false);
        
        when(swiftCodeRepository.findById("CITIUS12XXX"))
            .thenReturn(Optional.of(hq));
        when(swiftCodeRepository.findBySwiftCodeStartingWith("CITIUS12"))
            .thenReturn(Arrays.asList(hq, branch));

        // act
        SwiftCodeResponse response = swiftCodeService.getSwiftCodeDetails("CITIUS12XXX");

        // assert
        assertNotNull(response);
        assertNotNull(response.getBranches());
        assertEquals(1, response.getBranches().size());
    }

    @Test
    void shouldThrowExceptionForNonExistentSwiftCode() {
        when(swiftCodeRepository.findById(anyString()))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            swiftCodeService.getSwiftCodeDetails("INVALID123"));
    }

    @Test
    void shouldAddNewSwiftCode() {
        // arrange
        SwiftCodeRequest request = createTestSwiftCodeRequest();
        when(countryRepository.findById("US"))
            .thenReturn(Optional.empty());
        when(bankRepository.findBySwiftCode("CITIUS12"))
            .thenReturn(Optional.empty());

        // act
        String result = swiftCodeService.addSwiftCode(request);

        // assert
        assertEquals("SWIFT code added successfully", result);
        verify(swiftCodeRepository).save(any(SwiftCode.class));
    }

    @Test
    void shouldValidateSwiftCodeFormat() {
        SwiftCodeRequest invalidRequest = new SwiftCodeRequest(
            "INVALID",
            "BANK NAME",
            "PL",
            "COUNTRY NAME",
            "ADDRESS",
            true
        );
        assertThrows(SwiftCodeValidationException.class, () ->
            swiftCodeService.addSwiftCode(invalidRequest));
    }

    private SwiftCode createTestSwiftCode(String code, boolean isHq) {
        SwiftCode swiftCode = new SwiftCode();
        swiftCode.setSwiftCode(code);
        swiftCode.setHeadquarter(isHq);
        
        Bank bank = new Bank();
        bank.setBankName("TEST BANK");
        bank.setSwiftCode(code.substring(0, 8));
        swiftCode.setBank(bank);
        
        Country country = new Country();
        country.setIso2Code("US");
        country.setName("UNITED STATES");
        swiftCode.setCountry(country);
        
        return swiftCode;
    }

    private SwiftCodeRequest createTestSwiftCodeRequest() {
        SwiftCodeRequest request = new SwiftCodeRequest(
            "CITIUS12XXX",
            "CITIBANK NA",
            "US",
            "UNITED STATES",
            "TEST ADDRESS",
            true
        );
        return request;
    }
}