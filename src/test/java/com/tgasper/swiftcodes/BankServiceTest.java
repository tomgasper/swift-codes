package com.tgasper.swiftcodes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tgasper.swiftcodes.exception.SwiftCodeValidationException;
import com.tgasper.swiftcodes.model.Bank;
import com.tgasper.swiftcodes.repository.BankRepository;
import com.tgasper.swiftcodes.service.BankService;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {
    @Mock
    private BankRepository bankRepository;

    @InjectMocks
    private BankService bankService;

    @Test
    void shouldCreateNewBankWhenNotExists() {
        // arrange
        String baseSwiftCode = "BPKOPLPW";
        String bankName = "PKO BANK POLSKI";
        when(bankRepository.findBySwiftCode(baseSwiftCode))
            .thenReturn(Optional.empty());
        when(bankRepository.save(any(Bank.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // act
        Bank result = bankService.getOrCreateBank(baseSwiftCode, bankName);

        // assert
        assertNotNull(result);
        assertEquals(baseSwiftCode, result.getSwiftCode());
        assertEquals(bankName, result.getBankName());
        verify(bankRepository).save(any(Bank.class));
    }

    @Test
    void shouldReturnExistingBankWhenMatchesName() {
        // arrange
        String baseSwiftCode = "BPKOPLPW";
        String bankName = "PKO BANK POLSKI";
        Bank existingBank = new Bank();
        existingBank.setSwiftCode(baseSwiftCode);
        existingBank.setBankName(bankName);

        when(bankRepository.findBySwiftCode(baseSwiftCode))
            .thenReturn(Optional.of(existingBank));

        // act
        Bank result = bankService.getOrCreateBank(baseSwiftCode, bankName);

        // assert
        assertNotNull(result);
        assertEquals(baseSwiftCode, result.getSwiftCode());
        assertEquals(bankName, result.getBankName());
    }

    @Test
    void shouldThrowExceptionWhenBankNameMismatch() {
        // arrange
        String baseSwiftCode = "BPKOPLPW";
        String existingBankName = "PKO BANK POLSKI";
        String newBankName = "BANK PEKAO SA";
        
        Bank existingBank = new Bank();
        existingBank.setSwiftCode(baseSwiftCode);
        existingBank.setBankName(existingBankName);

        when(bankRepository.findBySwiftCode(baseSwiftCode))
            .thenReturn(Optional.of(existingBank));

        // act & assert
        SwiftCodeValidationException exception = assertThrows(
            SwiftCodeValidationException.class,
            () -> bankService.getOrCreateBank(baseSwiftCode, newBankName)
        );

        String expectedMessage = String.format(
            "Bank name mismatch. Existing bank name: %s, Provided bank name: %s",
            existingBankName, newBankName
        );
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldDeleteBank() {
        // arrange
        String baseSwiftCode = "BPKOPLPW";

        // act
        bankService.deleteBank(baseSwiftCode);

        // assert
        verify(bankRepository).deleteBySwiftCode(baseSwiftCode);
    }
} 